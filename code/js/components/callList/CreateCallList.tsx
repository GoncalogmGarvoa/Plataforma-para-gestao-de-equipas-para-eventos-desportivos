import * as React from "react";
import {useState, useEffect} from "react";
import {useNavigate} from "react-router-dom";
import { getCookie } from "../../src/context/Authn";

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

interface UserDetails {
    userId: number;
    name?: string;
    userName?: string;
    email?: string;
    phoneNumber?: string;
    [key: string]: any;
}

/*
export function getCookie(name: string): string | undefined {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) {
        return parts.pop()?.split(";").shift();
    }
    return undefined;
}
*/

export function CreateCallList() {
    const navigate = useNavigate();
    const [errorMessage, setErrorMessage] = useState<string | null>(null);
    const [showSuccessDialog, setShowSuccessDialog] = useState(false);

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

    // Equipment state
    const [equipmentOptions, setEquipmentOptions] = useState<{id: number, name: string}[]>([]);
    const [selectedEquipmentIds, setSelectedEquipmentIds] = useState<number[]>([]);

    // Dropdown state
    const [equipmentDropdownOpen, setEquipmentDropdownOpen] = useState(false);
    const equipmentDropdownRef = React.useRef<HTMLDivElement>(null);

    // Function dropdown state
    const [functionOptions, setFunctionOptions] = useState<{id: number, name: string}[]>([]);

    // Fechar dropdown ao clicar fora
    React.useEffect(() => {
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
                    headers: {
                        Authorization: `bearer ${getCookie("token")}`,
                    }
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

    // Fetch equipment options on mount
    React.useEffect(() => {
        const fetchEquipment = async () => {
            try {
                const token = getCookie("token");
                const res = await fetch("/arbnet/equipment", {
                    headers: {
                        Authorization: `bearer ${getCookie("token")}`,
                    }
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

    // Fetch function options on mount
    React.useEffect(() => {
        const fetchFunctions = async () => {
            try {
                const token = getCookie("token");
                const res = await fetch("/arbnet/users/functions", {
                    headers: {
                        Authorization: `bearer ${getCookie("token")}`,
                    }
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

    const [participantInputs, setParticipantInputs] = useState<Record<string, Record<string, string>>>({}); // name -> { date -> function }

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const {name, value} = e.target;
        setFormData((prev) => ({...prev, [name]: value}));
    };

    const addMatchDay = () => {
        setErrorMessage(null); // Clear any previous error messages
        if (!newDay || !newSessionTime) {
            setErrorMessage("Data e hora do dia da convocatória são obrigatórios.");
            return;
        }

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
        setErrorMessage(null); // Clear any previous error messages
        if (!newParticipantName || participantInputs[newParticipantName]) return;

        try {
            const token = getCookie("token");
            const res = await fetch(`/arbnet/users/name?name=${encodeURIComponent(newParticipantName)}`, {
                method: "GET",
                headers: {
                    Authorization: `bearer ${getCookie("token")}`,
                }
            });

            if (!res.ok) {
                const errorData = await res.json().catch((): null => null);
                console.error("Full response on addParticipant error:", res);
                console.error("Error data from API (addParticipant):", errorData);
                let errorMessageText = "Erro ao procurar utilizador.";
                if (errorData) {
                    errorMessageText = errorData.message || errorData.detail || errorData.title || JSON.stringify(errorData);
                } else {
                    errorMessageText = await res.text();
                }
                setErrorMessage(errorMessageText);
                return;
            }

            const users: { name: string, id: number }[] = await res.json();
            const foundUser = users.find(u => u.name.toLowerCase() === newParticipantName.toLowerCase());

            if (!foundUser) {
                setErrorMessage("Utilizador não encontrado.");
                return;
            }

            const userId = foundUser.id;

            setParticipantInputs((prev) => ({
                ...prev,
                [newParticipantName]: Object.fromEntries(matchDaySessionsInput.map(({matchDay}) => [matchDay, ""]))
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
            setErrorMessage("Erro ao buscar utilizador.");
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
        setErrorMessage(null); // Clear any previous error messages

        const requiredFields = [
            "competitionName",
            "address",
            "phoneNumber",
            "email",
            "association",
            "location",
            "deadline",
        ];

        for (const field of requiredFields) {
            if (!formData[field as keyof typeof formData]) {
                setErrorMessage(`O campo '${field}' é obrigatório.`);
                return;
            }
        }

        if (matchDaySessionsInput.length === 0) {
            setErrorMessage("É necessário adicionar pelo menos um Dia e Hora.");
            return;
        }

        if (selectedEquipmentIds.length === 0) {
            setErrorMessage("É necessário selecionar pelo menos um Equipamento.");
            return;
        }

        const token = getCookie("token");
        if (!token) {
            setErrorMessage("Token não encontrado. Faça login novamente.");
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
            equipmentIds: selectedEquipmentIds,
        };

        try {
            const response = await fetch("/arbnet/callList/creation", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `bearer ${getCookie("token")}`,
                },
                body: JSON.stringify(fullFormData)
            });

            if (!response.ok) {
                const errorData = await response.json().catch((): null => null); // Attempt to parse JSON, but don't fail if it's not JSON
                console.error("Full response on error:", response);
                console.error("Error data from API:", errorData);

                let errorMessageText = "Erro ao criar convocatória.";
                if (errorData) {
                    errorMessageText = errorData.message || errorData.detail || errorData.title || JSON.stringify(errorData);
                } else {
                    errorMessageText = await response.text(); // Get raw text if JSON parsing failed
                }
                setErrorMessage(errorMessageText);
                return;
            }

            setShowSuccessDialog(true); // Show success dialog
        } catch (err) {
            console.error(err);
            setErrorMessage(err instanceof Error ? err.message : "Erro inesperado.");
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

    const [showUserModal, setShowUserModal] = useState(false);
    const [selectedUserDetails, setSelectedUserDetails] = useState<UserDetails | null>(null);
    const [loadingUserDetails, setLoadingUserDetails] = useState(false);
    const [userDetailsError, setUserDetailsError] = useState<string | null>(null);

    const handleShowUserInfo = async (userId: number) => {
        setLoadingUserDetails(true);
        setUserDetailsError(null);
        setShowUserModal(true);
        try {
            const res = await fetch(`/arbnet/users/id/${userId}`,{
                headers: {
                    Authorization: `bearer ${getCookie("token")}`,
                }
            });
            if (!res.ok) throw new Error("Erro ao obter detalhes do utilizador");
            const data = await res.json();
            setSelectedUserDetails(data);
        } catch (err: any) {
            setUserDetailsError(err.message || "Erro desconhecido");
            setSelectedUserDetails(null);
        } finally {
            setLoadingUserDetails(false);
        }
    };

    return (
        <div className="create-call-list-container">
            <h2>Criar Convocatória</h2>

            {errorMessage && (
                <div style={{ color: 'red', marginBottom: '1rem' }}>
                    {errorMessage}
                </div>
            )}

            {showSuccessDialog && (
                <div style={{
                    position: 'fixed',
                    top: 0,
                    left: 0,
                    width: '100%',
                    height: '100%',
                    backgroundColor: 'rgba(0,0,0,0.5)',
                    display: 'flex',
                    justifyContent: 'center',
                    alignItems: 'center',
                    zIndex: 1000,
                }}>
                    <div style={{
                        backgroundColor: 'white',
                        padding: '2rem',
                        borderRadius: '8px',
                        boxShadow: '0 4px 16px rgba(0,0,0,0.2)',
                        textAlign: 'center',
                        width: '300px',
                    }}>
                        <h3>Sucesso!</h3>
                        <p>Convocatória criada com sucesso!</p>
                        <button onClick={() => {
                            setShowSuccessDialog(false);
                            navigate("/search-calllist-draft"); // Navigate on dialog close
                        }} style={{ marginTop: '1rem', padding: '0.5rem 1rem', backgroundColor: '#007bff', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>
                            OK
                        </button>
                    </div>
                </div>
            )}

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
                    <label>Conselho:</label>
                    <input name="association" onChange={handleChange}/>
                </div>
                <div className="form-group-inline">
                    <label>Estabelecimento:</label>
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

            <form onSubmit={(e) => { e.preventDefault(); handleSubmit(); }}>
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
                        {Object.entries(participantInputs).map(([name, rolesByDay], index) => {
                            const userId = nameToUserIdMap[name];
                            return (
                                <tr key={index}>
                                    <td>
                                        {name}
                                        {userId && (
                                            <button
                                                style={{ marginLeft: "0.5rem", background: "#007bff", color: "#fff", border: "none", borderRadius: 4, padding: "2px 10px", cursor: "pointer" }}
                                                type="button"
                                                onClick={() => handleShowUserInfo(userId)}
                                            >
                                                Info
                                            </button>
                                        )}
                                        <button
                                            style={{marginLeft: "0.5rem", color: "red"}}
                                            type="button"
                                            onClick={() => removeParticipant(name)}
                                        >
                                            Remover
                                        </button>
                                    </td>
                                    {matchDaySessionsInput.map((md) => (
                                        <td key={md.matchDay}>
                                            <select
                                                value={rolesByDay[md.matchDay] || ""}
                                                onChange={(e) => handleRoleChange(name, md.matchDay, e.target.value)}
                                                style={{width: "100%"}}
                                            >
                                                <option value="" disabled>Selecione uma Função</option>
                                                {functionOptions.map(func => (
                                                    <option key={func.id} value={func.name}>{func.name}</option>
                                                ))}
                                            </select>
                                        </td>
                                    ))}
                                </tr>
                            );
                        })}
                    </tbody>
                </table>

                <button type="submit">Criar Convocatória</button>
            </form>

            {/* Modal de detalhes do utilizador */}
            {showUserModal && (
                <div style={{
                    position: "fixed",
                    top: 0,
                    left: 0,
                    width: "100vw",
                    height: "100vh",
                    background: "rgba(0,0,0,0.3)",
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                    zIndex: 1000
                }}
                    onClick={() => setShowUserModal(false)}
                >
                    <div style={{
                        background: "#fff",
                        padding: 24,
                        borderRadius: 8,
                        minWidth: 320,
                        maxWidth: 400,
                        boxShadow: "0 2px 8px rgba(0,0,0,0.2)",
                        position: "relative"
                    }}
                        onClick={e => e.stopPropagation()}
                    >
                        <button style={{position: "absolute", top: 8, right: 8}} onClick={() => setShowUserModal(false)}>X</button>
                        <h3>Informação do Utilizador</h3>
                        {loadingUserDetails ? (
                            <p>A carregar...</p>
                        ) : userDetailsError ? (
                            <p style={{color: 'red'}}>{userDetailsError}</p>
                        ) : selectedUserDetails ? (
                            <div>
                                <p><b>Nome:</b> {selectedUserDetails.name || selectedUserDetails.userName}</p>
                                {selectedUserDetails.phoneNumber && <p><b>Número de Telemóvel:</b> {selectedUserDetails.phoneNumber}</p>}
                                {selectedUserDetails.email && <p><b>Email:</b> {selectedUserDetails.email}</p>}
                            </div>
                        ) : (
                            <p>Sem dados.</p>
                        )}
                    </div>
                </div>
            )}
        </div>
    );
}
