import * as React from "react";
import {useState} from "react";
import {useNavigate} from "react-router-dom";

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
        email: "",//getCookie("email") || "",
        association: "",
        location: "",
        deadline: "",
        callListType: "callList"
    });

    const [participants, setParticipants] = useState<ParticipantChoice[]>([]);
    const [matchDaySessionsInput, setMatchDaySessionsInput] = useState<MatchDaySessionsInput[]>([]);
    const [newParticipantName, setNewParticipantName] = useState<string>("");

    const [newDay, setNewDay] = useState<string>("");
    const [newSessionTime, setNewSessionTime] = useState<string>("");

    const [participantQuery, setParticipantQuery] = useState("");
    const [userSuggestions, setUserSuggestions] = useState<{ name: string; id: number }[]>([]);
    const [nameToUserIdMap, setNameToUserIdMap] = useState<Record<string, number>>({});



    React.useEffect(() => {
        const fetchUsers = async () => {
            if (participantQuery.length < 2) {
                setUserSuggestions([]);
                return;
            }

            try {
                const token = getCookie("token");
                const res = await fetch(`/arbnet/users/name?name=${encodeURIComponent(participantQuery)}`, {
                    method: "GET",
                    headers: {token}
                });

                if (!res.ok) throw new Error("Erro ao procurar utilizadores");

                const users: { name: string, id: number }[] = await res.json();
                setUserSuggestions(users);
            } catch (err) {
                console.error("Nenhum utilizador encontrado", err);
                setUserSuggestions([]);
            }
        };

        fetchUsers();
    }, [participantQuery]);


    const [participantInputs, setParticipantInputs] = useState<Record<string, Record<string, string>>>({}); // name -> { date -> function }

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const {name, value} = e.target;
        setFormData((prev) => ({...prev, [name]: value}));
    };

    const addMatchDay = () => {
        if (!newDay || !newSessionTime) return;

        setMatchDaySessionsInput((prev) => {
            const existing = prev.find(d => d.matchDay === newDay);
            if (existing) {
                // Se já existe o dia, adiciona a hora
                return prev.map(d =>
                    d.matchDay === newDay
                        ? {...d, sessions: [...new Set([...d.sessions, newSessionTime])]}
                        : d
                );
            } else {
                // Se não existe, adiciona novo dia com essa hora
                return [...prev, {matchDay: newDay, sessions: [newSessionTime]}];
            }
        });

        setNewDay("");
        setNewSessionTime("");
    };

    const addParticipant = async () => {
        if (!newParticipantName || participantInputs[newParticipantName]) return;

        try {
            const token = getCookie("token");
            const res = await fetch(`/arbnet/users/name?name=${encodeURIComponent(newParticipantName)}`, {
                method: "GET",
                headers: {token},
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
                [newParticipantName]: Object.fromEntries(matchDaySessionsInput.map(({matchDay}) => [matchDay, "DEFAULT"]))
            }));

            setParticipants((prev) => [
                ...prev,
                {
                    userId,
                    participantAndRole: matchDaySessionsInput.map(({matchDay}) => ({
                        matchDay: matchDay,
                        function: "DEFAULT"
                    }))
                }
            ]);

            setNameToUserIdMap((prev) => ({
                ...prev,
                [newParticipantName]: userId
            }));


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

        const matchDaySessions: MatchDaySessionsInput[] = matchDaySessionsInput;

        const updatedParticipants: ParticipantChoice[] = Object.entries(participantInputs).map(([name, rolesByDay]) => {
            const participantAndRole = Object.entries(rolesByDay).map(([matchDay, func]) => ({
                matchDay,
                function: func
            }));

            return {
                userId: nameToUserIdMap[name] ?? 0,
                participantAndRole
            };
        });

        const fullFormData: CallListInputModel = {
            ...formData,
            participants: updatedParticipants,
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


    const removeParticipant = (name: string) => {
        setParticipants((prev) =>
            prev.filter((p) => {
                const matchingName = Object.keys(participantInputs).find(
                    (key) => key === name
                );
                return matchingName
                    ? p.userId !==
                    participants.find((pt) => participantInputs[matchingName] && pt.userId === p.userId)?.userId
                    : true;
            })
        );

        setParticipantInputs((prev) => {
            const updated = {...prev};
            delete updated[name];
            return updated;
        });
    };


    return (
        <div className="create-call-list-container">
            <h2>Criar Convocatória</h2>

            <div className="form-inline-row">
                <div className="form-group-inline">
                    <label>Competição:</label>
                    <input name="competitionName" onChange={handleChange}/>
                </div>
                <div className="form-group-inline">
                    <label>Morada:</label>
                    <input name="address" onChange={handleChange}/>
                </div>
                <div className="form-group-inline">
                    <label>Telefone:</label>
                    <input name="phoneNumber" onChange={handleChange}/>
                </div>
                <div className="form-group-inline">
                    <label>Associação:</label>
                    <input name="association" onChange={handleChange}/>
                </div>
                <div className="form-group-inline">
                    <label>Local:</label>
                    <input name="location" onChange={handleChange}/>
                </div>
                <div className="form-group-inline">
                    <label>Email:</label>
                    <input name="email" onChange={handleChange}/>
                </div>
            </div>

            <div className="form-group-inline">
                <label>Data Limite:</label>
                <input className="deadline-input" name="deadline" type="date" onChange={handleChange}/>
            </div>


            <h3>Dias da Convocatória</h3>
            <div>
                <div className="form-inline-day">
                    <div className="form-group-day">
                        <label>Data:</label>
                        <input
                            type="date"
                            value={newDay}
                            onChange={(e) => setNewDay(e.target.value)}
                        />
                    </div>

                    <div className="form-group-day">
                        <label>Hora:</label>
                        <input
                            type="time"
                            value={newSessionTime}
                            onChange={(e) => setNewSessionTime(e.target.value)}
                        />
                    </div>
                </div>

                <div className="add-day-button-container">
                    <button onClick={addMatchDay}>Adicionar Dia e Hora</button>
                </div>
            </div>


            <ul style={{padding: 0, listStyle: "none"}}>
                {matchDaySessionsInput.flatMap(({matchDay, sessions}) =>
                    sessions.map((session) => (
                        <li key={`${matchDay}-${session}`}
                            style={{display: "flex", alignItems: "center", gap: "1rem", marginBottom: "0.5rem"}}>
                            <span>{matchDay}</span>
                            <span>{session}</span>
                            <button
                                className="remove-button"
                                onClick={() => {
                                    setMatchDaySessionsInput(prev =>
                                        prev
                                            .map(day =>
                                                day.matchDay === matchDay
                                                    ? {...day, sessions: day.sessions.filter(s => s !== session)}
                                                    : day
                                            )
                                            .filter(day => day.sessions.length > 0)
                                    );

                                    setParticipantInputs(prev => {
                                        const updated = {...prev};
                                        for (const name in updated) {
                                            if (updated[name][matchDay] !== undefined) {
                                                delete updated[name][matchDay];
                                            }
                                        }
                                        return updated;
                                    });

                                    setParticipants(prev =>
                                        prev.map(participant => ({
                                            ...participant,
                                            participantAndRole: participant.participantAndRole.filter(p => p.matchDay !== matchDay)
                                        }))
                                    );
                                }}
                            >
                                Remover
                            </button>

                        </li>
                    ))
                )}
            </ul>

            <h3>Participantes</h3>
            <div style={{position: "relative"}}>
                <label>Nomes </label>
                <input
                    value={participantQuery}
                    onChange={(e) => {
                        setParticipantQuery(e.target.value);
                        setNewParticipantName(e.target.value);
                    }}
                />
                {userSuggestions.length > 0 && (
                    <ul style={{
                        position: "absolute",
                        background: "white",
                        border: "1px solid #ccc",
                        padding: "0.5rem",
                        margin: 0,
                        listStyle: "none",
                        zIndex: 10,
                        maxHeight: "150px",
                        overflowY: "auto",
                        width: "100%"
                    }}>
                        {userSuggestions.map((user) => (
                            <li
                                key={user.id}
                                style={{cursor: "pointer", padding: "4px"}}
                                onClick={() => {
                                    setNewParticipantName(user.name);
                                    setParticipantQuery(user.name);
                                    setUserSuggestions([]);
                                }}
                            >
                                {user.name}
                            </li>
                        ))}
                    </ul>
                )}
            </div>
            <button onClick={addParticipant}>Adicionar Participante</button>

            <table border={1} cellPadding={5} style={{borderCollapse: "collapse", marginTop: "1rem"}}>
                <thead>
                <tr>
                    <th>Nome</th>
                    {matchDaySessionsInput.map(({matchDay}) => (
                        <th key={matchDay}>{matchDay}</th>
                    ))}
                </tr>
                </thead>
                <tbody>
                {Object.entries(participantInputs).map(([name, roles]) => (
                    <tr key={name}>
                        <td>
                            <strong>{name}</strong>
                            <button
                                className="remove-button"
                                style={{marginLeft: "8px"}}
                                onClick={() => removeParticipant(name)}
                            >
                                Remover
                            </button>

                        </td>
                        {matchDaySessionsInput.map(({matchDay}) => (
                            <td key={matchDay}>
                            <input
                                    value={roles[matchDay] || ""}
                                    onChange={(e) => handleRoleChange(name, matchDay, e.target.value)}
                                    placeholder="Função"
                                    style={{width: "100px"}}
                                />
                            </td>
                        ))}
                    </tr>

                ))}
                </tbody>
            </table>

            <div>
                <button onClick={handleSubmit}>Criar Convocatória</button>
            </div>
        </div>
    );
}
