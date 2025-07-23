import * as React from "react";
import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import "../../EditCallList.css"
import { useCurrentRole } from "../../src/context/Referee";

interface ParticipantInfo {
    userName: string
    category: string
    function: string
    confirmationStatus: string
    userId: number
    matchDayId: number
}

interface UserDetails {
    userId: number;
    name?: string;
    userName?: string;
    email?: string;
    phoneNumber?: string;
    [key: string]: any;
}

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
    const [dateToMatchDayIdMap, setDateToMatchDayIdMap] = useState<Record<string, number>>({});
    // MATCH DAYS
    const [matchDaySessionsInput, setMatchDaySessionsInput] = useState<any[]>([]); // [{matchDay, sessions: [hora]}]

    // Equipment dropdown state
    const [equipmentOptions, setEquipmentOptions] = useState<{id: number, name: string}[]>([]);
    const [selectedEquipmentIds, setSelectedEquipmentIds] = useState<number[]>([]);
    const [equipmentDropdownOpen, setEquipmentDropdownOpen] = useState(false);
    const equipmentDropdownRef = React.useRef<HTMLDivElement>(null);

    // Function dropdown state
    const [functionOptions, setFunctionOptions] = useState<{id: number, name: string}[]>([]);


    const [showUserModal, setShowUserModal] = useState(false);
    const [selectedUserDetails, setSelectedUserDetails] = useState<UserDetails | null>(null);
    const [loadingUserDetails, setLoadingUserDetails] = useState(false);
    const [userDetailsError, setUserDetailsError] = useState<string | null>(null);

    const [respondingUserId, setRespondingUserId] = useState<number | null>(null);


    function getStatusForParticipant(name: string, matchDayId: number): string | undefined {
        const participant = (form.participants.find(
            (p: ParticipantInfo) => p.userName === name && p.matchDayId === matchDayId
        ))
        return participant?.confirmationStatus;
    }

    function getStatusEmoji(status?: string): string {
        switch (status) {
            case "accepted":
                return "✔️";
            case "declined":
                return "❌";
            case "waiting":
                return "⏳";
            default:
                return "❔";
        }
    }


    // Carregar dados iniciais
    useEffect(() => {
        const fetchCallList = async () => {
            setLoading(true);
            setError(null);
            try {
                const response = await fetch(`/arbnet/callList/get/${id}`,{
                    headers: {
                        Authorization: `bearer ${getCookie("token")}`,
                    }
                });
                if (!response.ok) {
                    const err = await response.json();
                    throw new Error(err.title || "Erro ao buscar convocatória");
                }
                const data = await response.json();
                // Garante que callListId está presente
                setForm({ ...data, callListId: parseInt(id), callListType: data.callListType || "" });
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
                                const dateKeyRaw = md.matchDate || md.day || md.date || md.matchDay;
                                let dateKey = "";
                                if (dateKeyRaw) {
                                    try {
                                        const dateObj = new Date(dateKeyRaw);
                                        if (!isNaN(dateObj.getTime())) {
                                            dateKey = dateObj.toISOString().split('T')[0];
                                        }
                                    } catch (e) {
                                        console.error("Error parsing dateKey in fetchCallList:", e);
                                    }
                                }

                                if (dateKey) {
                                    // Procurar se este participante tem função para este dia específico
                                    const participantForThisDay = participantEntries.find((p: any) => p.matchDayId === md.id);
                                    
                                    if (participantForThisDay) {
                                        partInputs[name][dateKey] = participantForThisDay.functionName || "";
                                    } else {
                                        // Se não tem função atribuída para este dia, deixar vazio
                                        partInputs[name][dateKey] = "";
                                    }
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
                    // Corrigir sessions para garantir que é sempre [{ time: string }]
                    const fixedSessions = data.matchDaySessions.map((md: any) => ({
                        ...md,
                        sessions: (md.sessions || []).map((s: any) => {
                            if (typeof s === "string") {
                                return { time: s };
                            } else if (s && typeof s === "object" && s.time) {
                                return { time: s.time };
                            } else if (s && typeof s === "object" && s.startTime) {
                                return { time: s.startTime };
                            } else {
                                return { time: "" };
                            }
                        })
                    }));
                    setMatchDaySessionsInput(fixedSessions);

                    // Populate dateToMatchDayIdMap
                    const tempMap: Record<string, number> = {};
                    data.matchDaySessions.forEach((md: any) => {
                        const dateKeyRaw = md.matchDate || md.day || md.date || md.matchDay;
                        let dateKey = "";
                        if (dateKeyRaw) {
                            try {
                                const dateObj = new Date(dateKeyRaw);
                                if (!isNaN(dateObj.getTime())) {
                                    dateKey = dateObj.toISOString().split('T')[0];
                                    tempMap[dateKey] = md.id; // Store the ID
                                }
                            } catch (e) {
                                console.error("Error parsing dateKey for matchDayId map:", e);
                            }
                        }
                    });
                    setDateToMatchDayIdMap(tempMap);
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
                    headers: {
                        Authorization: `bearer ${getCookie("token")}`,
                    }
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
                headers: {
                    Authorization: `bearer ${getCookie("token")}`,
                }
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
                    matchDaySessionsInput.map((md: any) => {
                        const dateKeyRaw = md.matchDate; // Use matchDate directly
                        let dateKey = "";
                        if (dateKeyRaw) {
                            try {
                                const dateObj = new Date(dateKeyRaw);
                                if (!isNaN(dateObj.getTime())) {
                                    dateKey = dateObj.toISOString().split('T')[0];
                                }
                            } catch (e) {
                                console.error("Error parsing dateKey in addParticipant:", e);
                            }
                        }
                        return [dateKey, "DEFAULT"];
                    }).filter(([dateKey]) => dateKey !== "")
                )
            }));
    
            setParticipants((prev) => [
                ...prev,
                {
                    userId,
                    participantAndRole: matchDaySessionsInput.map((md: any) => ({
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
        setMatchDaySessionsInput((prevMatchDays) => {
            const oldMatchDay = prevMatchDays[index];
            const oldDateKey = oldMatchDay.matchDate; // The date BEFORE the change

            const updatedMatchDays = prevMatchDays.map((item, i) =>
                i === index ? { ...item, matchDate: value } : item // `value` is the new date
            );

            // Update participantInputs based on the changed date
            setParticipantInputs((prevParticipantInputs) => {
                const newParticipantInputs: Record<string, Record<string, string>> = {};
                Object.keys(prevParticipantInputs).forEach(name => {
                    const participantDays = { ...prevParticipantInputs[name] }; // Copy existing map for this participant

                    // Remove the old date entry if it exists for this participant
                    if (participantDays[oldDateKey]) {
                        delete participantDays[oldDateKey];
                    }

                    // Add the new date entry with "DEFAULT" for the changed match day
                    participantDays[value] = "DEFAULT";

                    newParticipantInputs[name] = participantDays;
                });
                return newParticipantInputs;
            });

            return updatedMatchDays;
        });
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
                    headers: {
                        Authorization: `bearer ${getCookie("token")}`,
                    }
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

    const handleCancelCallList = async () => {
        const confirmed = window.confirm("Tem a certeza que pretende cancelar esta convocatória?");
        if (!confirmed) return;

        try {
            const token = getCookie("token");
            if (!token) {
                setError("Token não encontrado. Faça login novamente.");
                return;
            }

            const response = await fetch("/arbnet/callList/cancel", {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `bearer ${getCookie("token")}`,
                },
                body: JSON.stringify({ callListId: form.callListId }) // 👈 atualizado aqui
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.title || "Erro ao cancelar convocatória.");
            }

            // sucesso — voltar à página principal
            navigate("/search-calllist-draft");

        } catch (err) {
            setError(err instanceof Error ? err.message : "Erro inesperado ao cancelar convocatória.");
        }
    };



    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setSubmitting(true);
        setError(null);

        if (!form || !form.callListId) {
            setError("ID da convocatória não encontrado.");
            setSubmitting(false);
            return;
        }

        if (!form.deadline) {
            setError("O prazo de resposta é obrigatório.");
            setSubmitting(false);
            return;
        }

        const requiredStringFields = [
            "competitionName",
            "address",
            "phoneNumber",
            "email",
            "association",
            "location",
            "callListType",
        ];

        for (const field of requiredStringFields) {
            if (!form[field] || String(form[field]).trim() === "") {
                setError(`O campo '${field}' é obrigatório.`);
                setSubmitting(false);
                return;
            }
        }

        // Construir matchDaySessions para o envio
        const formattedMatchDaySessions = matchDaySessionsInput.map(md => {
            // Ensure matchDay is consistently formatted as YYYY-MM-DD
            const dateValue = md.matchDate || md.day || md.date || md.matchDay;
            let matchDayFormatted = "";
            if (dateValue) {
                try {
                    const dateObj = new Date(dateValue);
                    if (!isNaN(dateObj.getTime())) {
                        matchDayFormatted = dateObj.toISOString().split('T')[0];
                    }
                } catch (e) {
                    console.error("Error parsing matchDay date:", e);
                }
            }

            if (!matchDayFormatted) return null; // If date parsing fails or is empty, filter this entry out

            return {
                matchDay: matchDayFormatted,
                sessions: md.sessions.map((s: any) => {
                    const timeValue = s.time;
                    if (timeValue) {
                        try {
                            // Parse time as HH:MM and format to HH:MM (adding leading zeros if necessary)
                            const [hours, minutes] = timeValue.split(':').map(Number);
                            if (!isNaN(hours) && !isNaN(minutes)) {
                                return `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}`;
                            }
                        } catch (e) {
                            console.error("Error parsing session time:", e);
                        }
                    }
                    return null; // Return null for invalid or empty times
                }).filter(Boolean), // Filter out nulls
            };
        }).filter(Boolean); // Remover entradas nulas

        const participantsMap = new Map<number, { matchDay: string; function: string }[]>();

        Object.entries(participantInputs).forEach(([userName, functionsByMatchDay]) => {
            Object.entries(functionsByMatchDay).forEach(([matchDayKey, functionName]) => {
                const userId = nameToUserIdMap[userName];
                if (userId !== undefined) {
                    if (!participantsMap.has(userId)) {
                        participantsMap.set(userId, []);
                    }
                    participantsMap.get(userId)?.push({
                        matchDay: matchDayKey,
                        function: functionName,
                    });
                }
            });
        });

        const formattedParticipants = Array.from(participantsMap.entries()).map(([userId, functionsByMatchDay]) => ({
            userId: userId,
            participantAndRole: functionsByMatchDay.map(item => ({
                matchDay: item.matchDay,
                function: item.function,
            })),
        }));

        const payload = {
            callListId: form.callListId,
            competitionName: form.competitionName,
            deadline: new Date(form.deadline).toISOString().split('T')[0],
            address: form.address,
            phoneNumber: form.phoneNumber,
            email: form.email,
            association: form.association,
            location: form.location,
            equipmentIds: selectedEquipmentIds,
            callListType: form.callListType, // Ensure callListType is always sent
            matchDaySessions: formattedMatchDaySessions,
            participants: formattedParticipants,
        };

        console.log("Payload being sent:", payload);

        try {
            const token = getCookie("token");
            const response = await fetch("/arbnet/callList/update", {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `bearer ${getCookie("token")}`,
                },
                body: JSON.stringify(payload),
            });

            if (!response.ok) {
                const err = await response.json();
                throw new Error(err.title || "Erro ao atualizar convocatória");
            }
            alert("Convocatória atualizada com sucesso!");
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
            const response = await fetch("/arbnet/callList/updateCallListStage", {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `bearer ${getCookie("token")}`,
                },
                body: JSON.stringify({ id: id }),
            });

            if (!response.ok) {
                const err = await response.json();
                throw new Error(err.title || "Erro ao selar convocatória");
            }
            alert("Convocatória selada com sucesso!");
            navigate("/search-calllist-draft");
        } catch (err: any) {
            setError(err.message || "Erro desconhecido ao selar convocatória");
            console.error("Erro ao selar convocatória:", err);
        } finally {
            setSubmitting(false);
        }
    }

    const currentRole = useCurrentRole();

    // Função para o conselho de arbitragem dar resposta para um participante
    const handleCouncilResponse = async (userId: number) => {
        const token = getCookie("token");
        if (!token) {
            alert("Token não encontrado. Faça login novamente.");
            return;
        }
        // Pega todos os dias desse participante
        const participant = participants.find(p => p.userId === userId);
        if (!participant) return;
        const days = (participant.participantAndRole || []).map((pr: any) => pr.matchDay);
        const input = {
            days,
            callListId: form.callListId,
            userId: userId
        };
        try {
            const res = await fetch("/arbnet/callList/updateParticipant/ArbitrationCouncil", {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `bearer ${getCookie("token")}`,
                },
                body: JSON.stringify(input)
            });
            if (!res.ok) throw new Error("Erro ao atualizar confirmação pelo conselho de arbitragem");
            alert("Confirmação do participante atualizada pelo conselho de arbitragem com sucesso!");
        } catch (err) {
            console.error(err);
            alert("Erro ao enviar confirmação pelo conselho de arbitragem.");
        }
    };

    // Adicionar função auxiliar para resposta do conselho por dia
    const handleCouncilResponseDay = async (userId: number, dayId: number, accept: boolean) => {
        const token = getCookie("token");
        if (!token) {
            alert("Token não encontrado. Faça login novamente.");
            return;
        }
        const input = {
            days: [{ dayId, status: accept ? 1 : 0 }],
            callListId: form.callListId,
            userId: userId
        };
        try {
            const res = await fetch("/arbnet/callList/updateParticipant/ArbitrationCouncil", {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `bearer ${getCookie("token")}`,
                },
                body: JSON.stringify(input)
            });
            if (!res.ok) throw new Error("Erro ao atualizar confirmação pelo conselho de arbitragem");
            await refetchCallList();
        } catch (err) {
            console.error(err);
            alert("Erro ao enviar resposta do conselho de arbitragem.");
        }
    };

    // Adicionar função auxiliar para refazer o fetch dos dados:
    const refetchCallList = async () => {
        setLoading(true);
        setError(null);
        try {
            const response = await fetch(`/arbnet/callList/get/${id}`,{
                headers: {
                    Authorization: `bearer ${getCookie("token")}`,
                }
            });
            if (!response.ok) {
                const err = await response.json();
                throw new Error(err.title || "Erro ao buscar convocatória");
            }
            const data = await response.json();
            // Corrigir sessions para garantir que é sempre [{ time: string }]
            const fixedSessions = data.matchDaySessions.map((md: any) => ({
                ...md,
                sessions: (md.sessions || []).map((s: any) => {
                    if (typeof s === "string") {
                        return { time: s };
                    } else if (s && typeof s === "object" && s.time) {
                        return { time: s.time };
                    } else if (s && typeof s === "object" && s.startTime) {
                        return { time: s.startTime };
                    } else {
                        return { time: "" };
                    }
                })
            }));
            setMatchDaySessionsInput(fixedSessions);
            // Replicar lógica de setForm, setParticipants, etc, se necessário
            setForm({ ...data, callListId: parseInt(id), callListType: data.callListType || "" });
            // ... (outros estados se necessário)
        } catch (err: any) {
            setError(err.message || "Erro inesperado");
        } finally {
            setLoading(false);
        }
    };

    if (loading) return <p>Loading...</p>;

    const isReadOnlyParticipants = form.callListType === 'sealedCallList' || form.callListType === 'finalJury';
    const seeAnswers = form.callListType !== 'callList'
    const hideActionButtons = form.callListType === 'finalJury';

    const allParticipantsResponded =
        form.participants.every((p: ParticipantInfo) => p.confirmationStatus !== "waiting")
        && form.deadline ;

    const canSealCallList = form.callListType !== "sealedCallList" ||
        (form.participants.every((p: ParticipantInfo) => p.confirmationStatus !== "waiting") ||
            new Date(form.deadline) < new Date())

    let a = "hello"


    return (
        <div className="edit-call-list-container">
            <h2>Editar Convocatória</h2>
            {error && <p className="error-message" style={{ color: 'red', marginBottom: '10px' }}>Error: {error}</p>}
            <form onSubmit={handleSubmit}>
                <div className="form-section">
                    <h3>Detalhes da Convocatória</h3>
                    <div className="form-grid">
                        <div className="form-group">
                            <label htmlFor="competitionName" className="form-label">Nome da Competição:</label>
                            <input
                                type="text"
                                id="competitionName"
                                className="form-input"
                                value={form.competitionName || ''}
                                onChange={(e) => setForm({ ...form, competitionName: e.target.value })}
                                required
                            />
                        </div>
                        <div className="form-group">
                            <label htmlFor="location" className="form-label">Localização:</label>
                            <input
                                type="text"
                                id="location"
                                className="form-input"
                                value={form.location || ''}
                                onChange={(e) => setForm({ ...form, location: e.target.value })}
                                required
                            />
                        </div>
                        <div className="form-group">
                            <label htmlFor="deadline" className="form-label">Prazo de Resposta:</label>
                            <input
                                type="date"
                                id="deadline"
                                className="form-input"
                                value={form.deadline ? new Date(form.deadline).toISOString().substring(0, 10) : ''}
                                onChange={(e) => setForm({ ...form, deadline: e.target.value })}
                                required
                            />
                        </div>
                        <div className="form-group">
                            <label htmlFor="address" className="form-label">Morada:</label>
                            <input
                                type="text"
                                id="address"
                                className="form-input"
                                value={form.address || ''}
                                onChange={(e) => setForm({ ...form, address: e.target.value })}
                                required
                            />
                        </div>
                        <div className="form-group">
                            <label htmlFor="phoneNumber" className="form-label">Número de Telefone:</label>
                            <input
                                type="text"
                                id="phoneNumber"
                                className="form-input"
                                value={form.phoneNumber || ''}
                                onChange={(e) => setForm({ ...form, phoneNumber: e.target.value })}
                                required
                            />
                        </div>
                        <div className="form-group">
                            <label htmlFor="email" className="form-label">Email:</label>
                            <input
                                type="email"
                                id="email"
                                className="form-input"
                                value={form.email || ''}
                                onChange={(e) => setForm({ ...form, email: e.target.value })}
                                required
                            />
                        </div>
                        <div className="form-group">
                            <label htmlFor="association" className="form-label">Associação:</label>
                            <input
                                type="text"
                                id="association"
                                className="form-input"
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
                                            className="form-input match-day-date-input"
                                            value={md.matchDate ? new Date(md.matchDate).toISOString().substring(0, 10) : ''}
                                            onChange={(e) => handleMatchDayChange(mdIndex, e.target.value)}
                                            required
                                        />
                                    </td>
                                    <td>
                                        <div className="sessions-list" style={{ display: 'flex', flexWrap: 'wrap', gap: '5px', alignItems: 'center' }}>
                                            {md.sessions.map((session: any, sessionIndex: number) => {
                                                console.log('Session debug:', session);
                                                return (
                                                    <div key={sessionIndex} className="session-item" style={{ display: 'flex', alignItems: 'center', gap: '5px' }}>
                                                        <input
                                                            type="time"
                                                            className="form-input session-time-input"
                                                            style={{ width: '100px' }}
                                                            value={session.time ? session.time.slice(0,5) : ""}
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
                                                );
                                            })}
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
                        <label htmlFor="equipment" className="form-label">Equipamentos Necessários:</label>
                        <input
                            type="text"
                            id="equipment"
                            className="form-input"
                            placeholder="Selecione equipamentos"
                            value={form.equipments ? form.equipments.map((eq: any) => eq.name).join(', ') : ''}
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
                    <div className="participants-table-container">
                        <table className="matchday-table">
                            <thead>
                                <tr>
                                    <th>Nome</th>
                                    {matchDaySessionsInput.map((md, index) => (
                                        <th key={index}>{new Date(md.matchDate).toLocaleDateString()}</th>
                                    ))}
                                    <th>Ações</th>
                                </tr>
                            </thead>
                            <tbody>
                                {Object.entries(participantInputs).map(([name, days]) => {
                                    const userId = nameToUserIdMap[name];
                                    return (
                                        <tr key={name}>
                                            <td>
                                                {name}
                                                {userId && (
                                                    <button
                                                        style={{ marginLeft: "0.5rem", color: "#007bff" }}
                                                        type="button"
                                                        onClick={() => handleShowUserInfo(userId)}
                                                    >
                                                        Info
                                                    </button>
                                                )}
                                            </td>
                                            {matchDaySessionsInput.map((md) => {
                                                const dateKey = md.matchDate;
                                                return (
                                                    <td key={dateKey}>
                                                        <div className="flex flex-col gap-1">
                                                            <select
                                                                className="form-input"
                                                                value={days[dateKey] || ""}
                                                                onChange={(e) => handleRoleChange(name, dateKey, e.target.value)}
                                                                disabled={isReadOnlyParticipants}
                                                            >
                                                                <option value="">Selecione Função</option>
                                                                {functionOptions.map((func) => (
                                                                    <option key={func.id} value={func.name}>
                                                                        {func.name}
                                                                    </option>
                                                                ))}
                                                            </select>

                                                        {seeAnswers && (
                                                            <div className="mt-1 text-lg">
                                                                {getStatusEmoji(getStatusForParticipant(name, md.id))}
                                                                {currentRole === "Arbitration_Council" && respondingUserId === userId && (
                                                                    <>
                                                                        <button
                                                                            type="button"
                                                                            className="btn btn-success btn-xs"
                                                                            style={{ marginLeft: 4, fontSize: 12, padding: '2px 8px' }}
                                                                            onClick={async () => {
                                                                                await handleCouncilResponseDay(userId, md.id, true);
                                                                            }}
                                                                        >
                                                                            Aceitar
                                                                        </button>
                                                                        <button
                                                                            type="button"
                                                                            className="btn btn-danger btn-xs"
                                                                            style={{ marginLeft: 4, fontSize: 12, padding: '2px 8px' }}
                                                                            onClick={async () => {
                                                                                await handleCouncilResponseDay(userId, md.id, false);
                                                                            }}
                                                                        >
                                                                            Recusar
                                                                        </button>
                                                                    </>
                                                                )}
                                                            </div>
                                                        )}
                                                    </div>
                                                </td>
                                            );
                                        })}
                                        <td>
                                            <button

                                                type="button"
                                                className="btn btn-danger btn-sm"
                                                onClick={() => removeParticipant(name)}
                                                disabled={isReadOnlyParticipants}
                                            >
                                                Remover
                                            </button>
                                            {currentRole === "Arbitration_Council" && isReadOnlyParticipants && (
                                                <button
                                                    type="button"
                                                    className="btn btn-info btn-sm"
                                                    style={{ marginLeft: 8 }}
                                                    onClick={() => setRespondingUserId(respondingUserId === userId ? null : userId)}
                                                >
                                                    Responder pelo Utilizador
                                                </button>
                                            )}
                                        </td>
                                    </tr>
                                )})}
                            </tbody>
                        </table>
                    </div>
                    {!isReadOnlyParticipants && (
                        <div className="add-participant-section" style={{ display: 'flex', alignItems: 'flex-end', gap: '10px', marginTop: '30px', marginBottom: '15px' }}>
                            <input
                                type="text"
                                className="form-input"
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
                                Adicionar
                            </button>
                        </div>
                    )}

                    {!isReadOnlyParticipants && userSuggestions.length > 0 && participantQuery.length > 1 && (
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

                {!hideActionButtons && (
                    <div className="button-group">
                        {error && <p className="error-message" style={{ color: 'red', marginBottom: '10px' }}>Error: {error}</p>}
                        <button type="submit" className="btn btn-success" disabled={submitting}>
                            {submitting ? 'Aguarde...' : 'Atualizar Convocatória'}
                        </button>
                        <button
                            type="button"
                            className="btn btn-info"
                            onClick={handleSealCallList}
                            disabled={!canSealCallList}
                        >
                            Lacrar Convocatória
                        </button>

                        <button
                            type="button"
                            className="btn btn-danger"
                            onClick={handleCancelCallList}
                        >
                            Cancelar
                        </button>

                    </div>
                )}

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
                        <button style={{position: "absolute", top: 8, right: 8}}
                                onClick={() => setShowUserModal(false)}>X
                        </button>
                        <h3>Informação do Utilizador</h3>
                        {loadingUserDetails ? (
                            <p>A carregar...</p>
                        ) : userDetailsError ? (
                            <p style={{color: 'red'}}>{userDetailsError}</p>
                        ) : selectedUserDetails ? (
                            <div>
                                <p><b>Nome:</b> {selectedUserDetails.name || selectedUserDetails.userName}</p>
                                {selectedUserDetails.phoneNumber &&
                                    <p><b>Número de Telemóvel:</b> {selectedUserDetails.phoneNumber}</p>}
                                {selectedUserDetails.email && <p><b>Email:</b> {selectedUserDetails.email}</p>}
                            </div>
                        ) : (
                            <p>Sem dados.</p>
                        )}
                    </div>
                </div>
            )}
        </div>
    )
} 