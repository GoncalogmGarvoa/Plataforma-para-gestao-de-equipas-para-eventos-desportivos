import * as React from "react";
import { useState } from "react";
import { useNavigate } from "react-router-dom";

interface ParticipantChoice {
    userId: number;
    participantAndRole: FunctionByMatchDayDto[];
}

interface FunctionByMatchDayDto {
    matchDay: string;
    function: string;
}

interface MatchDaySessionsInput {
    matchDay: string;
    sessions: string[];
}

interface CallListInputModel {
    callListId?: number;
    competitionName: string;
    address: string;
    phoneNumber: string;
    email: string;
    association: string;
    location: string;
    userId: number;
    participants: ParticipantChoice[];
    deadline: string;
    callListType: string;
    matchDaySessions: MatchDaySessionsInput[];
    equipmentIds: number[];
}

function getCookie(name: string): string | undefined {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) {
        return parts.pop()?.split(";").shift();
    }
    return undefined;
}

export function CreateCallList() {
    const navigate = useNavigate();

    const [formData, setFormData] = useState<Omit<CallListInputModel, "participants" | "matchDaySessions" | "equipmentIds">>({
        competitionName: "",
        address: "",
        phoneNumber: "",
        email: getCookie("email") || "",
        association: "",
        location: "",
        userId: parseInt(getCookie("userId") || "10"),
        deadline: "",
        callListType: ""
    });

    const [participants, setParticipants] = useState<ParticipantChoice[]>([]);
    const [matchDays, setMatchDays] = useState<string[]>([]);
    const [newDay, setNewDay] = useState<string>("");
    const [newParticipantName, setNewParticipantName] = useState<string>("");

    const [participantInputs, setParticipantInputs] = useState<Record<string, Record<string, string>>>({}); // name -> { date -> function }

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        setFormData((prev) => ({ ...prev, [name]: value }));
    };

    const addMatchDay = () => {
        if (newDay && !matchDays.includes(newDay)) {
            setMatchDays([...matchDays, newDay]);
            setNewDay("");
        }
    };

    const addParticipant = async () => {
        if (!newParticipantName || participantInputs[newParticipantName]) return;

        try {
            const token = getCookie("token");
            const res = await fetch(`/arbnet/users/name?name=${encodeURIComponent(newParticipantName)}`, {
                method: "GET",
                headers: { token },
            });


            if (!res.ok) throw new Error("Utilizador não foi encontrado.");

            const users: { name: string, id: number }[] = await res.json();
            const foundUser = users.find(u => u.name.toLowerCase() === newParticipantName.toLowerCase());

            if (!foundUser) {
                alert("Utilizador não encontrado.");
                return;
            }

            const userId = foundUser.id;

            setParticipantInputs((prev) => ({
                ...prev,
                [newParticipantName]: Object.fromEntries(matchDays.map(day => [day, "DEFAULT"]))
            }));

            setParticipants((prev) => [
                ...prev,
                {
                    userId,
                    participantAndRole: matchDays.map(day => ({
                        matchDay: day,
                        function: "DEFAULT"
                    }))
                }
            ]);

            setNewParticipantName("");
        } catch (error) {
            console.error(error);
            alert("Erro ao buscar utilizador.");
        }
    };

    const handleRoleChange = (name: string, day: string, func: string) => {
        setParticipantInputs((prev) => ({
            ...prev,
            [name]: {
                ...prev[name],
                [day]: func
            }
        }));
    };

    const handleSubmit = async () => {
        const token = getCookie("token");
        if (!token) {
            alert("Token não encontrado. Faça login novamente.");
            return;
        }

        const matchDaySessions: MatchDaySessionsInput[] = matchDays.map(day => ({
            matchDay: day,
            sessions: ["10:00"] // podes tornar isto editável depois
        }));

        const fullFormData : CallListInputModel= {
            ...formData,
            participants,
            matchDaySessions,
            equipmentIds: []
        };

        try {
                const response = await fetch("/arbnet/callList/creation", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    token
                },
                body: JSON.stringify(fullFormData)
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.title || "Erro ao criar convocatória.");
            }

            alert("Convocatória criada com sucesso!");
            navigate("/");
        } catch (err) {
            console.error(err);
            alert(err instanceof Error ? err.message : "Erro inesperado.");
        }
    };

    return (
        <div>
            <h2>Criar Convocatória</h2>
            <input name="competitionName" placeholder="Competição" onChange={handleChange} />
            <input name="address" placeholder="Morada" onChange={handleChange} />
            <input name="phoneNumber" placeholder="Telefone" onChange={handleChange} />
            <input name="association" placeholder="Associação" onChange={handleChange} />
            <input name="location" placeholder="Local" onChange={handleChange} />
            <input name="deadline" type="date" onChange={handleChange} />
            <input name="callListType" placeholder="Tipo de convocatória" onChange={handleChange} />

            <h3>Dias da Convocatória</h3>
            <input type="date" value={newDay} onChange={(e) => setNewDay(e.target.value)} />
            <button onClick={addMatchDay}>Adicionar Dia</button>

            <h3>Participantes</h3>
            <input value={newParticipantName} placeholder="Nome do participante" onChange={(e) => setNewParticipantName(e.target.value)} />
            <button onClick={addParticipant}>Adicionar Participante</button>

            {Object.entries(participantInputs).map(([name, roles]) => (
                <div key={name}>
                    <strong>{name}</strong>
                    {matchDays.map(day => (
                        <div key={day}>
                            <input
                                value={day}
                                disabled
                                style={{ width: "120px", marginRight: "5px" }}
                            />
                            <input
                                value={roles[day]}
                                onChange={(e) => handleRoleChange(name, day, e.target.value)}
                                placeholder="Função"
                                style={{ width: "100px" }}
                            />
                        </div>
                    ))}
                </div>
            ))}

            <button onClick={handleSubmit}>Criar</button>
        </div>
    );
}
