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

    // Equipment dropdown state
    const [equipmentOptions, setEquipmentOptions] = useState<{id: number, name: string}[]>([]);
    const [selectedEquipmentIds, setSelectedEquipmentIds] = useState<number[]>([]);
    const [equipmentDropdownOpen, setEquipmentDropdownOpen] = useState(false);
    const equipmentDropdownRef = React.useRef<HTMLDivElement>(null);

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
                        return [dateKey, "DEFAULT"];
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
                const userId = nameToUserIdMap[name] ?? 0;
                const participantAndRole = Object.entries(rolesByDay).map(([date, functionName]) => {
                    const matchDay = form.matchDaySessions.find((md: any) =>
                        md.matchDate === date || md.day === date || md.date === date || md.matchDay === date
                    );
                    return {
                        matchDay: matchDay?.matchDate,
                        function: functionName
                    };
                });
                // Garante que participantAndRole é sempre array (mesmo vazio)
                return {
                    userId,
                    participantAndRole: participantAndRole || []
                };
            });
            
            const matchDaySessions = (form.matchDaySessions || []).map((md: any) => ({
                ...md,
                matchDay: md.matchDate, // ou o campo correto de data
                sessions: (md.sessions || []).map((s: any) => {
                    if (typeof s === "string") {
                        // Verifica se já está no formato HH:mm:ss
                        if (/^\d{2}:\d{2}(:\d{2})?$/.test(s)) {
                            return s.length === 5 ? s + ":00" : s;
                        }
                        const d = new Date(`1970-01-01T${s}`);
                        if (!isNaN(d.getTime())) {
                            return d.toTimeString().slice(0, 8);
                        }
                        throw new Error(`Formato de hora inválido: ${s}`);
                    }
                    if (s instanceof Date) {
                        return s.toTimeString().slice(0, 8);
                    }
                    if (typeof s === "object" && s.startTime) {
                        // Usa o startTime do objeto
                        if (/^\d{2}:\d{2}(:\d{2})?$/.test(s.startTime)) {
                            return s.startTime.length === 5 ? s.startTime + ":00" : s.startTime;
                        }
                        // Se vier como string mas não está no formato, tenta converter
                        const d = new Date(`1970-01-01T${s.startTime}`);
                        if (!isNaN(d.getTime())) {
                            return d.toTimeString().slice(0, 8);
                        }
                        throw new Error(`Formato de hora inválido em startTime: ${s.startTime}`);
                    }
                    throw new Error(`Formato de sessão inválido: ${JSON.stringify(s)}`);
                })
            }));
            
            const fullFormData: any = {
                ...form,
                participants: updatedParticipants,
                matchDaySessions: matchDaySessions,
                equipmentIds: selectedEquipmentIds,
                callListId: form.callListId || form.id || id
            };
            const response = await fetch("/arbnet/callList/update", {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json",
                    token,
                },
                body: JSON.stringify(fullFormData),
            });
            if (!response.ok) {
                const err = await response.json();
                throw new Error(err.title || "Erro ao atualizar convocatória");
            }
            alert("Convocatória atualizada com sucesso!");
        } catch (err: any) {
            setError(err.message || "Erro inesperado");
        } finally {
            setSubmitting(false);
        }
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
                setEquipmentOptions([]);
            }
        };
        fetchEquipment();
    }, []);

    // Sync selectedEquipmentIds with form.equipmentIds ou form.equipment
    useEffect(() => {
        if (form) {
            if (Array.isArray(form.equipmentIds)) {
                setSelectedEquipmentIds(form.equipmentIds);
            } else if (Array.isArray(form.equipment)) {
                setSelectedEquipmentIds(form.equipment.map((eq: any) => eq.id));
            }
        }
        // eslint-disable-next-line
    }, [form]);

    // Fechar dropdown ao clicar fora
    useEffect(() => {
        function handleClickOutside(event: MouseEvent) {
            if (equipmentDropdownRef.current && !equipmentDropdownRef.current.contains(event.target as Node)) {
                setEquipmentDropdownOpen(false);
            }
        }
        if (equipmentDropdownOpen) {
            document.addEventListener('mousedown', handleClickOutside);
        } else {
            document.removeEventListener('mousedown', handleClickOutside);
        }
        return () => {
            document.removeEventListener('mousedown', handleClickOutside);
        };
    }, [equipmentDropdownOpen]);

    useEffect(() => {
        if (form) {
            console.log('EditCallList form:', form);
        }
    }, [form]);

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
                {/* Equipment Dropdown */}
                <div className="form-group-inline">
                    <label>Equipamentos:</label>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                        <div ref={equipmentDropdownRef} style={{ position: 'relative', width: 220 }}>
                            <button
                                type="button"
                                style={{ width: '100%', padding: '6px 10px', borderRadius: 4, border: '1px solid #ccc', background: '#fff', textAlign: 'left', cursor: 'pointer' }}
                                onClick={() => setEquipmentDropdownOpen(open => !open)}
                            >
                                {selectedEquipmentIds.length === 0 ? 'Selecione equipamento(s)' : `${selectedEquipmentIds.length} selecionado(s)`}
                                <span style={{ float: 'right' }}>▼</span>
                            </button>
                            {equipmentDropdownOpen && (
                                <div style={{
                                    position: 'absolute',
                                    top: '110%',
                                    left: 0,
                                    width: '100%',
                                    background: '#fff',
                                    border: '1px solid #ccc',
                                    borderRadius: 4,
                                    boxShadow: '0 2px 8px rgba(0,0,0,0.08)',
                                    zIndex: 20,
                                    maxHeight: 180,
                                    overflowY: 'auto',
                                }}>
                                    {equipmentOptions.filter(eq => !selectedEquipmentIds.includes(eq.id)).length === 0 ? (
                                        <div style={{ padding: 8, color: '#888' }}>Sem opções</div>
                                    ) : equipmentOptions.filter(eq => !selectedEquipmentIds.includes(eq.id)).map(eq => (
                                        <div
                                            key={eq.id}
                                            style={{ padding: '8px 12px', cursor: 'pointer', borderBottom: '1px solid #f0f0f0' }}
                                            onClick={() => {
                                                setSelectedEquipmentIds(prev => [...prev, eq.id]);
                                                setEquipmentDropdownOpen(false);
                                            }}
                                        >
                                            {eq.name}
                                        </div>
                                    ))}
                                </div>
                            )}
                        </div>
                        <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8, marginTop: 6 }}>
                            {selectedEquipmentIds.map(id => {
                                const eq = equipmentOptions.find(e => e.id === id);
                                if (!eq) return null;
                                return (
                                    <span key={id} style={{ background: '#e6f0ff', border: '1px solid #007bff', borderRadius: 4, padding: '2px 8px', display: 'flex', alignItems: 'center', gap: 4 }}>
                                        {eq.name}
                                        <button
                                            type="button"
                                            style={{ marginLeft: 4, color: 'red', border: 'none', background: 'transparent', cursor: 'pointer', fontWeight: 'bold' }}
                                            onClick={() => setSelectedEquipmentIds(prev => prev.filter(eid => eid !== id))}
                                        >
                                            ×
                                        </button>
                                    </span>
                                );
                            })}
                        </div>
                    </div>
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
                <table border={1} cellPadding={5} style={{ borderCollapse: "collapse", marginTop: "1rem", width: "100%" }}>
                    <thead>
                        <tr>
                        <th>Nome</th>
                        {form.matchDaySessions.map((md: any) => (
                            <th key={md.id}>{new Date(md.matchDate).toLocaleDateString("pt-PT")}</th>
                        ))}
                        </tr>
                    </thead>
                    <tbody>
                        {Object.entries(participantInputs).map(([name, rolesByDay], index) => (
                        <tr key={index}>
                            <td>
                            {name}
                            <button
                                style={{ marginLeft: "0.5rem", color: "red" }}
                                type="button"
                                onClick={() => removeParticipant(name)}
                            >
                                Remover
                            </button>
                            </td>
                            {form.matchDaySessions.map((md: any) => (
                            <td key={md.id}>
                                <input
                                type="text"
                                value={rolesByDay[md.matchDate] || ""}
                                onChange={(e) => handleRoleChange(name, md.matchDate, e.target.value)}
                                placeholder="Função"
                                style={{ width: "100%" }}
                                />
                            </td>
                            ))}
                        </tr>
                        ))}
                    </tbody>
                    </table>

                <button type="submit" disabled={submitting}>{submitting ? "Salvando..." : "Salvar Alterações"}</button>
            </form>
        </div>
    );
} 