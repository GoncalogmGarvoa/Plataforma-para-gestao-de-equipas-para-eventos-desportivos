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
                i === index ? { ...item, matchDate: value } : item
            )
        );
    };

    const handleSessionInputChange = (matchDayIndex: number, sessionIndex: number, value: string) => {
        setMatchDaySessionsInput((prev) =>
            prev.map((item, i) => {
                if (i === matchDayIndex) {
                    const newSessions = [...item.sessions];
                    newSessions[sessionIndex] = { time: value };
                    return { ...item, sessions: newSessions };
                }
                return item;
            })
        );
    };

    const addMatchDay = () => {
        setMatchDaySessionsInput((prev) => [...prev, { matchDate: "", sessions: [""] }]);
    };

    const addSession = (index: number) => {
        setMatchDaySessionsInput((prev) =>
            prev.map((item, i) =>
                i === index ? { ...item, sessions: [...item.sessions, { time: "" }] } : item
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
                            <label htmlFor="location">Localização:</label>
                            <input
                                type="text"
                                id="location"
                                className="form-control"
                                value={form.location || ''}
                                onChange={(e) => setForm({ ...form, location: e.target.value })}
                                required
                            />
                        </div>
                        <div className="form-group">
                            <label htmlFor="deadline">Prazo de Resposta:</label>
                            <input
                                type="date"
                                id="deadline"
                                className="form-control"
                                value={form.deadline ? new Date(form.deadline).toISOString().substring(0, 10) : ''}
                                onChange={(e) => setForm({ ...form, deadline: e.target.value })}
                                required
                            />
                        </div>
                        <div className="form-group">
                            <label htmlFor="competitionName">Nome da Competição:</label>
                            <input
                                type="text"
                                id="competitionName"
                                className="form-control"
                                value={form.competitionName || ''}
                                onChange={(e) => setForm({ ...form, competitionName: e.target.value })}
                                required
                            />
                        </div>
                        <div className="form-group">
                            <label htmlFor="address">Morada:</label>
                            <input
                                type="text"
                                id="address"
                                className="form-control"
                                value={form.address || ''}
                                onChange={(e) => setForm({ ...form, address: e.target.value })}
                                required
                            />
                        </div>
                        <div className="form-group">
                            <label htmlFor="phoneNumber">Número de Telefone:</label>
                            <input
                                type="text"
                                id="phoneNumber"
                                className="form-control"
                                value={form.phoneNumber || ''}
                                onChange={(e) => setForm({ ...form, phoneNumber: e.target.value })}
                                required
                            />
                        </div>
                        <div className="form-group">
                            <label htmlFor="email">Email:</label>
                            <input
                                type="email"
                                id="email"
                                className="form-control"
                                value={form.email || ''}
                                onChange={(e) => setForm({ ...form, email: e.target.value })}
                                required
                            />
                        </div>
                        <div className="form-group">
                            <label htmlFor="association">Associação:</label>
                            <input
                                type="text"
                                id="association"
                                className="form-control"
                                value={form.association || ''}
                                onChange={(e) => setForm({ ...form, association: e.target.value })}
                                required
                            />
                        </div>
                    </div>
                    <h3 className="section-title" style={{ marginTop: '30px' }}>Dia de Prova</h3>
                    <table className="matchday-table">
                        <thead>
                            <tr>
                                <th>Data da Prova</th>
                                <th>Sessões (horas)</th>
                                <th>Ações</th>
                            </tr>
                        </thead>
                        <tbody>
                            {matchDaySessionsInput.map((md, mdIndex) => (
                                <tr key={mdIndex}>
                                    <td>
                                        <input
                                            type="date"
                                            id={`matchDay-${mdIndex}`}
                                            className="form-control form-control-sm match-day-date-input"
                                            value={md.matchDate ? new Date(md.matchDate).toISOString().substring(0, 10) : ''}
                                            onChange={(e) => handleMatchDayChange(mdIndex, e.target.value)}
                                            required
                                        />
                                    </td>
                                    <td>
                                        <div className="sessions-list" style={{ display: 'flex', flexWrap: 'wrap', gap: '5px', alignItems: 'center' }}>
                                            {md.sessions.map((session: any, sessionIndex: number) => (
                                                <div key={sessionIndex} className="session-item" style={{ display: 'flex', alignItems: 'center', gap: '5px' }}>
                                                    <input
                                                        type="time"
                                                        className="form-control form-control-sm session-time-input"
                                                        style={{ width: '100px' }}
                                                        value={session.time || ''}
                                                        onChange={(e) => handleSessionInputChange(mdIndex, sessionIndex, e.target.value)}
                                                        required
                                                    />
                                                    <button
                                                        type="button"
                                                        className="btn btn-danger btn-sm remove-session-btn"
                                                        onClick={() => removeSession(mdIndex, sessionIndex)}
                                                    >
                                                        -
                                                    </button>
                                                </div>
                                            ))}
                                            <button
                                                type="button"
                                                className="btn btn-secondary btn-sm add-session-btn"
                                                onClick={() => addSession(mdIndex)}
                                            >
                                                +
                                            </button>
                                        </div>
                                    </td>
                                    <td>
                                        <button
                                            type="button"
                                            className="btn btn-danger btn-sm remove-match-day-btn"
                                            onClick={() => removeMatchDay(mdIndex)}
                                        >
                                            Remover Dia
                                        </button>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                    <button
                        type="button"
                        className="btn btn-secondary btn-sm add-match-day-btn-bottom"
                        onClick={addMatchDay}
                    >
                        Adicionar Dia de Prova
                    </button>
                    <h3 className="section-title" style={{ marginTop: '30px' }}>Equipamentos</h3>
                    <div className="form-group dropdown-container" ref={equipmentDropdownRef}>
                        <label htmlFor="equipment">Equipamentos Necessários:</label>
                        <input
                            type="text"
                            id="equipment"
                            className="form-control"
                            placeholder="Selecione equipamentos"
                            value={selectedEquipmentIds.map(id => equipmentOptions.find(opt => opt.id === id)?.name).filter(Boolean).join(', ')}
                            onFocus={() => setEquipmentDropdownOpen(true)}
                            readOnly
                        />
                        <button type="button" className="dropdown-toggle-button" onClick={() => setEquipmentDropdownOpen(!equipmentDropdownOpen)}>
                            {equipmentDropdownOpen ? '▲' : '▼'}
                        </button>
                        {equipmentDropdownOpen && (
                            <div className="dropdown-menu show">
                                {equipmentOptions.map((equipment) => (
                                    <div key={equipment.id} className="dropdown-item">
                                        <input
                                            type="checkbox"
                                            id={`equipment-${equipment.id}`}
                                            checked={selectedEquipmentIds.includes(equipment.id)}
                                            onChange={() => handleEquipmentChange(equipment.id)}
                                        />
                                        <label htmlFor={`equipment-${equipment.id}`}>{equipment.name}</label>
                                    </div>
                                ))}
                            </div>
                        )}
                    </div>
                    <div className="selected-equipment-tags" style={{ display: 'flex', flexWrap: 'wrap', gap: '8px', marginTop: '10px' }}>
                        {selectedEquipmentIds.map((id) => {
                            const eq = equipmentOptions.find((e) => e.id === id);
                            return eq ? (
                                <span key={id} className="equipment-tag" style={{ backgroundColor: '#007bff', borderRadius: '4px', padding: '5px 10px', display: 'flex', alignItems: 'center', gap: '5px', color: '#fff' }}>
                                    {eq.name}
                                    <button
                                        type="button"
                                        onClick={() => handleEquipmentChange(id)}
                                        style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#fff', fontSize: '14px' }}
                                    >
                                        x
                                    </button>
                                </span>
                            ) : null;
                        })}
                    </div>

                    <h3 className="section-title" style={{ marginTop: '30px' }}>Participantes e Funções</h3>
                    {Object.entries(participantInputs).map(([name, days]) => (
                        <div key={name} className="participant-item">
                            <h4>{name}</h4>
                            <button
                                type="button"
                                className="btn btn-danger btn-sm"
                                onClick={() => removeParticipant(name)}
                            >
                                Remover Participante
                            </button>
                            <div className="participant-roles-grid">
                                {matchDaySessionsInput.map((md) => {
                                    const dateKey = md.matchDate; // Use matchDate directly
                                    return (
                                        <div key={dateKey} className="participant-role-item">
                                            <label>{new Date(dateKey).toLocaleDateString()}:</label>
                                            <select
                                                className="form-control"
                                                value={days[dateKey] || ""}
                                                onChange={(e) => handleRoleChange(name, dateKey, e.target.value)}
                                            >
                                                <option value="">Selecione Função</option>
                                                {functionOptions.map((func) => (
                                                    <option key={func.id} value={func.name}>
                                                        {func.name}
                                                    </option>
                                                ))}
                                            </select>
                                        </div>
                                    );
                                })}
                            </div>
                        </div>
                    ))}

                    <div className="add-participant-section" style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '15px' }}>
                        <input
                            type="text"
                            className="form-control"
                            placeholder="Adicionar Participante (Nome)"
                            value={newParticipantName}
                            onChange={(e) => setNewParticipantName(e.target.value)}
                            onInput={(e) => setParticipantQuery((e.target as HTMLInputElement).value)}
                        />
                        <button
                            type="button"
                            className="btn btn-success btn-sm"
                            onClick={addParticipant}
                        >
                            Adicionar Participante
                        </button>
                    </div>
                    {userSuggestions.length > 0 && participantQuery.length > 1 && (
                        <ul className="suggestions-list" style={{ listStyle: 'none', padding: '0', margin: '0', border: '1px solid #ccc', borderRadius: '4px', maxHeight: '150px', overflowY: 'auto', zIndex: '1000', backgroundColor: '#fff' }}>
                            {userSuggestions.map((user) => (
                                <li
                                    key={user.id}
                                    onClick={() => {
                                        setNewParticipantName(user.name);
                                        setUserSuggestions([]);
                                    }}
                                    style={{ padding: '8px 12px', cursor: 'pointer' }}
                                >
                                    {user.name}
                                </li>
                            ))}
                        </ul>
                    )}
                </div>

                <div className="button-group">
                    <button type="submit" className="btn btn-success" disabled={submitting}>
                        {submitting ? 'Aguarde...' : 'Atualizar Convocatória'}
                    </button>
                    <button type="button" className="btn btn-info" onClick={handleSealCallList}>
                        Fechar Convocatória
                    </button>
                    <button type="button" className="btn btn-danger" onClick={() => navigate('/calllists-draft')}>
                        Cancelar
                    </button>
                </div>
            </form>
        </div>
    )
} 