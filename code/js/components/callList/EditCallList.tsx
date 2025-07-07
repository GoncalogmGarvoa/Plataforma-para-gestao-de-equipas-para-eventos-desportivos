import * as React from "react";
import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import "../../EditCallList.css"

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

    // Equipment dropdown state
    const [equipmentOptions, setEquipmentOptions] = useState<{id: number, name: string}[]>([]);
    const [selectedEquipmentIds, setSelectedEquipmentIds] = useState<number[]>([]);
    const [equipmentDropdownOpen, setEquipmentDropdownOpen] = useState(false);
    const equipmentDropdownRef = React.useRef<HTMLDivElement>(null);

    // Function dropdown state
    const [functionOptions, setFunctionOptions] = useState<{id: number, name: string}[]>([]);

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
                // Garante que callListId está presente
                if (!data.callListId && data.id) {
                    data.callListId = data.id;
                }
                setForm(data);
                // Popular equipamentos selecionados
                if (data.equipmentIds) {
                    setSelectedEquipmentIds(data.equipmentIds);
                }
                // Converter participantes para o formato de edição
                if (data.participants) {
                    const nameMap: Record<string, number> = {};
                    const idToName: Record<number, string> = {};
                    const partInputs: Record<string, Record<string, string>> = {};
                    const partArr: any[] = [];
                    
                    // Criar um mapa de participantes por userId para facilitar o acesso
                    const participantsByUserId = data.participants.reduce((acc: any, p: any) => {
                        if (!acc[p.userId]) {
                            acc[p.userId] = [];
                        }
                        acc[p.userId].push(p);
                        return acc;
                    }, {});
                    
                    // Para cada participante único, criar entradas para todos os dias disponíveis
                    Object.keys(participantsByUserId).forEach((userId) => {
                        const participantEntries = participantsByUserId[userId];
                        const firstParticipant = participantEntries[0];
                        const name = firstParticipant.userName || firstParticipant.name || userId;
                        
                        nameMap[name] = parseInt(userId);
                        idToName[parseInt(userId)] = name;
                        partInputs[name] = {};
                        
                        // Para cada dia disponível, verificar se o participante tem função atribuída
                        if (data.matchDaySessions) {
                            data.matchDaySessions.forEach((md: any) => {
                                const dateKey = md.matchDate || md.day || md.date || md.matchDay;
                                
                                // Procurar se este participante tem função para este dia específico
                                const participantForThisDay = participantEntries.find((p: any) => p.matchDayId === md.id);
                                
                                if (participantForThisDay) {
                                    partInputs[name][dateKey] = participantForThisDay.functionName || "";
                                } else {
                                    // Se não tem função atribuída para este dia, deixar vazio
                                    partInputs[name][dateKey] = "";
                                }
                            });
                        }
                        
                        partArr.push({
                            userId: parseInt(userId),
                            participantAndRole: participantEntries
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

    // Fetch function options on mount
    useEffect(() => {
        const fetchFunctions = async () => {
            try {
                const token = getCookie("token");
                const res = await fetch("/arbnet/users/functions", {
                    headers: token ? { token } : undefined
                });
                if (!res.ok) throw new Error("Erro ao buscar funções");
                const data = await res.json();
                setFunctionOptions(data);
            } catch (err) {
                console.error("Failed to fetch functions:", err);
                setFunctionOptions([]);
            }
        };
        fetchFunctions();
    }, []);

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
                [newParticipantName]: Object.fromEntries(
                    form.matchDaySessions.map((md: any) => {
                        const dateKey = md.matchDate || md.day || md.date || md.matchDay;
                        return [dateKey, ""];
                    })
                )
            }));
    
            setParticipants((prev) => [
                ...prev,
                {
                    userId,
                    participantAndRole: form.matchDaySessions.map((md: any) => ({
                        matchDay: md.matchDay,
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

    const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
        const { name, value } = e.target;
        setForm((prev: any) => ({ ...prev, [name]: value }));
    };

    const handleMatchDayChange = (index: number, value: string) => {
        setMatchDaySessionsInput((prev) =>
            prev.map((item, i) =>
                i === index ? { ...item, matchDay: value } : item
            )
        );
    };

    const handleSessionInputChange = (matchDayIndex: number, sessionIndex: number, value: string) => {
        setMatchDaySessionsInput((prev) =>
            prev.map((item, i) => {
                if (i === matchDayIndex) {
                    const newSessions = [...item.sessions];
                    newSessions[sessionIndex] = value;
                    return { ...item, sessions: newSessions };
                }
                return item;
            })
        );
    };

    const addMatchDay = () => {
        setMatchDaySessionsInput((prev) => [...prev, { matchDay: "", sessions: ["", ""] }]);
    };

    const addSession = (index: number) => {
        setMatchDaySessionsInput((prev) =>
            prev.map((item, i) =>
                i === index ? { ...item, sessions: [...item.sessions, ""] } : item
            )
        );
    };

    const removeMatchDay = (index: number) => {
        setMatchDaySessionsInput((prev) => prev.filter((_, i) => i !== index));
    };

    const removeSession = (matchDayIndex: number, sessionIndex: number) => {
        setMatchDaySessionsInput((prev) =>
            prev.map((item, i) =>
                i === matchDayIndex
                    ? { ...item, sessions: item.sessions.filter((_: string, si: number) => si !== sessionIndex) }
                    : item
            )
        );
    };

    const handleEquipmentChange = (equipmentId: number) => {
        setSelectedEquipmentIds((prev) =>
            prev.includes(equipmentId)
                ? prev.filter((id) => id !== equipmentId)
                : [...prev, equipmentId]
        );
    };

    // Fetch equipment options on mount
    useEffect(() => {
        const fetchEquipment = async () => {
            try {
                const token = getCookie("token");
                const res = await fetch("/arbnet/equipment", {
                    headers: token ? { token } : undefined
                });
                if (!res.ok) throw new Error("Erro ao buscar equipamentos");
                const data = await res.json();
                setEquipmentOptions(data);
            } catch (err) {
                console.error("Failed to fetch equipment:", err);
                setEquipmentOptions([]);
            }
        };
        fetchEquipment();
    }, []);

    // Click outside handler for equipment dropdown
    useEffect(() => {
        function handleClickOutside(event: MouseEvent) {
            if (equipmentDropdownRef.current && !equipmentDropdownRef.current.contains(event.target as Node)) {
                setEquipmentDropdownOpen(false);
            }
        }
        document.addEventListener("mousedown", handleClickOutside);
        return () => {
            document.removeEventListener("mousedown", handleClickOutside);
        };
    }, [equipmentDropdownRef]);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setSubmitting(true);
        setError(null);

        if (!form || !form.callListId) {
            setError("ID da convocatória não encontrado.");
            setSubmitting(false);
            return;
        }

        // Construir matchDaySessions para o envio
        const formattedMatchDaySessions = matchDaySessionsInput.map(md => {
            const matchDate = md.matchDay || md.matchDate || md.day || md.date;
            if (!matchDate) return null; // Ou lidar com erro

            return {
                matchDay: matchDate,
                sessions: md.sessions.filter((s: string) => s && s.trim() !== ""),
            };
        }).filter(Boolean); // Remover entradas nulas

        // Construir participants para o envio
        const formattedParticipants = Object.keys(participantInputs).flatMap(name => {
            const userId = nameToUserIdMap[name];
            if (!userId) return []; // Should not happen if logic is correct

            return Object.keys(participantInputs[name]).map(dateKey => {
                const functionName = participantInputs[name][dateKey];
                const matchDayObj = matchDaySessionsInput.find(md => (md.matchDay || md.matchDate || md.day || md.date) === dateKey);
                const matchDayId = matchDayObj?.id; // Assuming matchDayObj has an id

                return {
                    userId,
                    userName: name,
                    matchDayId,
                    functionName,
                };
            }).filter(p => p.functionName); // Only include if a function is assigned
        });

        const payload = {
            callListId: form.callListId,
            competitionName: form.competitionName,
            date: form.date,
            deadline: form.deadline,
            local: form.local,
            description: form.description,
            equipmentIds: selectedEquipmentIds,
            matchDaySessions: formattedMatchDaySessions,
            participants: formattedParticipants,
        };

        try {
            const token = getCookie("token");
            const response = await fetch("/arbnet/callList/edit", {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json",
                    ...(token && { token }),
                },
                body: JSON.stringify(payload),
            });

            if (!response.ok) {
                const err = await response.json();
                throw new Error(err.title || "Erro ao atualizar convocatória");
            }
            alert("Convocatória atualizada com sucesso!");
            navigate("/check-callLists");
        } catch (err: any) {
            setError(err.message || "Erro desconhecido ao atualizar");
            console.error("Erro ao submeter convocatória:", err);
        } finally {
            setSubmitting(false);
        }
    };

    async function handleSealCallList() {
        setSubmitting(true);
        setError(null);
        try {
            const token = getCookie("token");
            const response = await fetch(`/arbnet/callList/seal/${id}`, {
                method: "PUT",
                headers: {
                    ...(token && { token }),
                },
            });

            if (!response.ok) {
                const err = await response.json();
                throw new Error(err.title || "Erro ao selar convocatória");
            }
            alert("Convocatória selada com sucesso!");
            navigate("/check-callLists");
        } catch (err: any) {
            setError(err.message || "Erro desconhecido ao selar convocatória");
            console.error("Erro ao selar convocatória:", err);
        } finally {
            setSubmitting(false);
        }
    }

    if (loading) return <p>Loading...</p>;
    if (error) return <p>Error: {error}</p>;

    return (
        <div className="edit-call-list-container">
            <h2>Editar Convocatória</h2>
            <form onSubmit={handleSubmit}>
                <div className="form-section">
                    <h3>Detalhes da Convocatória</h3>
                    <div className="form-grid">
                        <div className="form-group">
                            <label htmlFor="competitionName" className="form-label">Nome da Competição</label>
                            <input
                                type="text"
                                id="competitionName"
                                name="competitionName"
                                value={form.competitionName || ""}
                                onChange={handleChange}
                                className="form-input"
                                required
                            />
                        </div>
                        <div className="form-group">
                            <label htmlFor="date" className="form-label">Data do Evento</label>
                            <input
                                type="date"
                                id="date"
                                name="date"
                                value={form.date || ""}
                                onChange={handleChange}
                                className="form-input"
                                required
                            />
                        </div>
                        <div className="form-group">
                            <label htmlFor="deadline" className="form-label">Prazo de Resposta</label>
                            <input
                                type="datetime-local"
                                id="deadline"
                                name="deadline"
                                value={form.deadline ? new Date(form.deadline).toISOString().slice(0, 16) : ""}
                                onChange={handleChange}
                                className="form-input"
                                required
                            />
                        </div>
                        <div className="form-group">
                            <label htmlFor="local" className="form-label">Local</label>
                            <input
                                type="text"
                                id="local"
                                name="local"
                                value={form.local || ""}
                                onChange={handleChange}
                                className="form-input"
                                required
                            />
                        </div>
                    </div>
                    <div className="form-group single-column">
                        <label htmlFor="description" className="form-label">Descrição</label>
                        <textarea
                            id="description"
                            name="description"
                            value={form.description || ""}
                            onChange={handleChange}
                            className="form-input"
                            rows={3}
                        />
                    </div>

                    <div className="form-group">
                        <label className="form-label">Equipamentos Necessários</label>
                        <div className="equipment-dropdown-container" ref={equipmentDropdownRef}>
                            <div
                                className={`equipment-dropdown-header ${equipmentDropdownOpen ? "open" : ""}`}
                                onClick={() => setEquipmentDropdownOpen(!equipmentDropdownOpen)}
                            >
                                <span>
                                    {selectedEquipmentIds.length === 0
                                        ? "Selecionar equipamentos"
                                        : `Selecionados: ${selectedEquipmentIds.length}`}
                                </span>
                                <span>{equipmentDropdownOpen ? "▲" : "▼"}</span>
                            </div>
                            {equipmentDropdownOpen && (
                                <div className="equipment-dropdown-list">
                                    {equipmentOptions.map((eq) => (
                                        <label key={eq.id}>
                                            <input
                                                type="checkbox"
                                                checked={selectedEquipmentIds.includes(eq.id)}
                                                onChange={() => handleEquipmentChange(eq.id)}
                                            />
                                            {eq.name}
                                        </label>
                                    ))}
                                </div>
                            )}
                        </div>
                        <div className="selected-equipment-tags">
                            {selectedEquipmentIds.map((id) => {
                                const eq = equipmentOptions.find((e) => e.id === id);
                                return eq ? (
                                    <span key={id} className="equipment-tag">
                                        {eq.name}
                                        <button type="button" onClick={() => handleEquipmentChange(id)}>
                                            x
                                        </button>
                                    </span>
                                ) : null;
                            })}
                        </div>
                    </div>
                </div>

                <div className="form-section">
                    <h3>Dias e Sessões</h3>
                    <table className="matchday-table">
                        <thead>
                        <tr>
                            <th>Data do Dia de Jogo</th>
                            <th>Sessões (horas)</th>
                            <th>Ações</th>
                        </tr>
                        </thead>
                        <tbody>
                        {matchDaySessionsInput.map((md, index) => (
                            <tr key={index}>
                                <td>
                                    <input
                                        type="date"
                                        value={md.matchDay || md.matchDate || md.day || md.date || ""}
                                        onChange={(e) => handleMatchDayChange(index, e.target.value)}
                                        className="form-input"
                                        required
                                    />
                                </td>
                                <td>
                                    {md.sessions.map((session: string, sIndex: number) => (
                                        <div key={sIndex} style={{ display: "flex", marginBottom: "0.5rem", alignItems: "center" }}>
                                            <input
                                                type="time"
                                                value={session || ""}
                                                onChange={(e) => handleSessionInputChange(index, sIndex, e.target.value)}
                                                className="form-input"
                                                required
                                            />
                                            <button
                                                type="button"
                                                onClick={() => removeSession(index, sIndex)}
                                                className="btn-danger"
                                                style={{ marginLeft: "0.5rem", padding: "0.3em 0.6em" }}
                                            >
                                                -X
                                            </button>
                                        </div>
                                    ))}
                                    <button type="button" onClick={() => addSession(index)} className="btn btn-secondary">
                                        Adicionar Sessão
                                    </button>
                                </td>
                                <td>
                                    <button type="button" onClick={() => removeMatchDay(index)} className="btn btn-danger">
                                        Remover Dia
                                    </button>
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                    <button type="button" onClick={addMatchDay} className="btn btn-primary">
                        Adicionar Dia de Jogo
                    </button>
                </div>

                <div className="form-section">
                    <h3>Participantes</h3>
                    <div className="add-participant-section">
                        <input
                            type="text"
                            placeholder="Nome do Participante"
                            value={newParticipantName}
                            onChange={(e) => setNewParticipantName(e.target.value)}
                            className="form-input search-input"
                        />
                        <button type="button" onClick={addParticipant} className="btn btn-primary">
                            Adicionar Participante
                        </button>
                    </div>
                    {userSuggestions.length > 0 && newParticipantName.length >= 2 && (
                        <ul className="suggestions-list">
                            {userSuggestions.map((user) => (
                                <li key={user.id} onClick={() => setNewParticipantName(user.name)}>
                                    {user.name}
                                </li>
                            ))}
                        </ul>
                    )}

                    {participants.length > 0 && (
                        <table className="participant-table">
                            <thead>
                            <tr>
                                <th>Nome do Participante</th>
                                {matchDaySessionsInput.map((md, index) => (
                                    <th key={index}>{md.matchDay || md.matchDate || md.day || md.date}</th>
                                ))}
                                <th>Ações</th>
                            </tr>
                            </thead>
                            <tbody>
                            {Object.keys(participantInputs).map((name) => (
                                <tr key={name}>
                                    <td>{name}</td>
                                    {matchDaySessionsInput.map((md, index) => {
                                        const dateKey = md.matchDay || md.matchDate || md.day || md.date;
                                        return (
                                            <td key={index}>
                                                <select
                                                    value={participantInputs[name][dateKey] || ""}
                                                    onChange={(e) =>
                                                        handleRoleChange(name, dateKey, e.target.value)
                                                    }
                                                    className="form-select"
                                                >
                                                    <option value="">Selecionar Função</option>
                                                    {functionOptions.map(func => (
                                                        <option key={func.id} value={func.name}>
                                                            {func.name}
                                                        </option>
                                                    ))}
                                                </select>
                                            </td>
                                        );
                                    })}
                                    <td>
                                        <button
                                            type="button"
                                            onClick={() => removeParticipant(name)}
                                            className="remove-participant-btn"
                                        >
                                            X
                                        </button>
                                    </td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    )}
                </div>

                <div className="button-group">
                    <button
                        type="submit"
                        className="btn btn-primary"
                        disabled={submitting}
                    >
                        {submitting ? "Salvando..." : "Salvar Convocatória"}
                    </button>
                    <button
                        type="button"
                        onClick={handleSealCallList}
                        className="btn btn-secondary"
                        disabled={submitting}
                    >
                        Selar Convocatória
                    </button>
                </div>
            </form>
        </div>
    )
} 