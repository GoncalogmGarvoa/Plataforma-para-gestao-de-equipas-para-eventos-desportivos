import * as React from "react";
import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";

function getCookie(name: string): string | undefined {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) {
        return parts.pop()?.split(";").shift();
    }
    return undefined;
}

export function EditCallList() {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [form, setForm] = useState<any>(null);
    const [submitting, setSubmitting] = useState(false);

    // PARTICIPANTES
    const [participantInputs, setParticipantInputs] = useState<Record<string, Record<string, string>>>({}); // name -> { date -> function }
    const [participants, setParticipants] = useState<any[]>([]); // [{userId, participantAndRole: [{matchDay, function}]}]
    const [nameToUserIdMap, setNameToUserIdMap] = useState<Record<string, number>>({});
    const [userIdToNameMap, setUserIdToNameMap] = useState<Record<number, string>>({});
    const [participantQuery, setParticipantQuery] = useState("");
    const [userSuggestions, setUserSuggestions] = useState<{ name: string; id: number }[]>([]);
    const [newParticipantName, setNewParticipantName] = useState<string>("");
    // MATCH DAYS
    const [matchDaySessionsInput, setMatchDaySessionsInput] = useState<any[]>([]); // [{matchDay, sessions: [hora]}]

    // Carregar dados iniciais
    useEffect(() => {
        const fetchCallList = async () => {
            setLoading(true);
            setError(null);
            try {
                const response = await fetch(`/arbnet/callList/get/${id}`);
                if (!response.ok) {
                    const err = await response.json();
                    throw new Error(err.title || "Erro ao buscar convocatória");
                }
                const data = await response.json();
                setForm(data);
                // Converter participantes para o formato de edição
                if (data.participants) {
                    const nameMap: Record<string, number> = {};
                    const idToName: Record<number, string> = {};
                    const partInputs: Record<string, Record<string, string>> = {};
                    const partArr: any[] = [];
                    data.participants.forEach((p: any) => {
                        const name = p.name || p.userName || p.userId?.toString() || "";
                        nameMap[name] = p.userId;
                        idToName[p.userId] = name;
                        partInputs[name] = {};
                        if (p.participantAndRole) {
                            p.participantAndRole.forEach((pr: any) => {
                                partInputs[name][pr.matchDay] = pr.function;
                            });
                        }
                        partArr.push({
                            userId: p.userId,
                            participantAndRole: p.participantAndRole || []
                        });
                    });
                    setNameToUserIdMap(nameMap);
                    setUserIdToNameMap(idToName);
                    setParticipantInputs(partInputs);
                    setParticipants(partArr);
                }
                // Converter dias e sessões
                if (data.matchDaySessions) {
                    setMatchDaySessionsInput(data.matchDaySessions);
                }
            } catch (err: any) {
                setError(err.message || "Erro inesperado");
            } finally {
                setLoading(false);
            }
        };
        if (id) fetchCallList();
    }, [id]);

    // Sugestão de utilizadores
    useEffect(() => {
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
                setUserSuggestions([]);
            }
        };
        fetchUsers();
    }, [participantQuery]);

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

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setForm({ ...form, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setSubmitting(true);
        setError(null);
        try {
            const token = getCookie("token");
            if (!token) throw new Error("Token não encontrado. Faça login novamente.");
            // Montar participantes para envio
            const updatedParticipants = Object.entries(participantInputs).map(([name, rolesByDay]) => {
                const participantAndRole = Object.entries(rolesByDay).map(([matchDay, func]) => ({
                    matchDay,
                    function: func
                }));
                return {
                    userId: nameToUserIdMap[name] ?? 0,
                    participantAndRole
                };
            });
            const updatedForm = {
                ...form,
                participants: updatedParticipants,
                matchDaySessions: matchDaySessionsInput
            };
            const response = await fetch("/arbnet/callList/update", {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json",
                    token,
                },
                body: JSON.stringify(updatedForm),
            });
            if (!response.ok) {
                const err = await response.json();
                throw new Error(err.title || "Erro ao atualizar convocatória");
            }
            navigate("/search-calllist-draft");
        } catch (err: any) {
            setError(err.message || "Erro inesperado");
        } finally {
            setSubmitting(false);
        }
    };

    if (loading) return <div>Carregando...</div>;
    if (error) return <div>Erro: {error}</div>;
    if (!form) return null;

    return (
        <div className="create-call-list-container">
            <h2>Editar Convocatória</h2>
            <form onSubmit={handleSubmit}>
                <div className="form-group-inline">
                    <label>Competição:</label>
                    <input name="competitionName" value={form.competitionName || ''} onChange={handleChange} />
                </div>
                <div className="form-group-inline">
                    <label>Morada:</label>
                    <input name="address" value={form.address || ''} onChange={handleChange} />
                </div>
                <div className="form-group-inline">
                    <label>Telefone:</label>
                    <input name="phoneNumber" value={form.phoneNumber || ''} onChange={handleChange} />
                </div>
                <div className="form-group-inline">
                    <label>Associação:</label>
                    <input name="association" value={form.association || ''} onChange={handleChange} />
                </div>
                <div className="form-group-inline">
                    <label>Local:</label>
                    <input name="location" value={form.location || ''} onChange={handleChange} />
                </div>
                <div className="form-group-inline">
                    <label>Email:</label>
                    <input name="email" value={form.email || ''} onChange={handleChange} />
                </div>
                <div className="form-group-inline">
                    <label>Data Limite:</label>
                    <input className="deadline-input" name="deadline" type="date" value={form.deadline || ''} onChange={handleChange} />
                </div>
                {/* PARTICIPANTES */}
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
                <button type="button" onClick={addParticipant}>Adicionar Participante</button>
                <table border={1} cellPadding={5} style={{borderCollapse: "collapse", marginTop: "1rem"}}>
                    <thead>
                    <tr>
                        <th>Nome</th>
                        <th>Função</th>
                        <th>Data</th>
                        <th>Ações</th>
                    </tr>
                    </thead>
                        <tbody>
                        {form.participants && form.matchDaySessions && form.participants.map((participant: any, index: number) => {
                        const matchDay = form.matchDaySessions.find((md: any) => md.id === participant.matchDayId);
                        const dataDia = matchDay?.matchDate || '';

                        return (
                            <tr key={`${participant.userId}-${participant.matchDayId}-${index}`}>
                                <td>{participant.userName}</td>
                                <td>
                                    <input
                                        value={participant.functionName || ''}
                                        onChange={e => {
                                            const newValue = e.target.value;
                                            setForm((prev: any) => ({
                                                ...prev,
                                                participants: prev.participants.map((p: any, i: number) =>
                                                    i === index ? { ...p, functionName: newValue } : p
                                                )
                                            }));
                                        }}
                                        placeholder="Função"
                                        style={{ width: "100px" }}
                                    />
                                </td>
                                <td>{new Date(dataDia).toLocaleDateString('pt-PT')}</td>
                                <td>
                                    <button
                                        className="remove-button"
                                        type="button"
                                        onClick={() => {
                                            setForm((prev: any) => ({
                                                ...prev,
                                                participants: prev.participants.filter((_: any, i: number) => i !== index)
                                            }));
                                        }}
                                    >
                                        Remover
                                    </button>
                                </td>
                            </tr>
                        );
                    })}



                    </tbody>

                </table>
                <button type="submit" disabled={submitting}>{submitting ? "Salvando..." : "Salvar Alterações"}</button>
            </form>
        </div>
    );
} 