import React, { useState, useEffect } from "react"
import { useLocation } from "react-router-dom"
import "../../CallListInfo.css"
import { getCookie } from "../../src/context/Authn"
import { useCurrentRole } from "../../src/context/Referee"

interface Session {
    id: number
    startTime: string
    endTime: string | null
    matchDayId: number
    competitionIdMatchDay: number
}

interface MatchDay {
    id: number
    matchDate: string
    competitionId: number
    sessions: Session[]
}

interface ParticipantInfo {
    name: string
    category: string
    function: string
    status: string
    userId: number
    matchDayId: number
    confirmationStatus?: string // <- campo opcional
}

interface EquipmentOutputModel {
    id: number
    name: string
}

interface RefereeCallListsOutputModel {
    callListId: number
    competitionName: string
    address: string
    phoneNumber: string
    email: string
    association: string
    location: string
    deadline: string
    callListType: string
    participants: ParticipantInfo[]
    matchDaySessions: MatchDay[]
    equipments: EquipmentOutputModel[]
}

interface MatchDayConfirmation {
    dayId: number;
    status: number;
}
interface ParticipantUpdateInput {
    days: MatchDayConfirmation[];
    callListId: number;
}

export function CallListInfo() {
    const location = useLocation()
    const [event, setEvent] = useState(location.state?.event as RefereeCallListsOutputModel)
    const participantId = location.state?.participantId as number

    // Estado sincronizado com event
    const [dayResponses, setDayResponses] = useState<Record<number, "accepted" | "declined" | "waiting">>(
        () => Object.fromEntries((event?.matchDaySessions || []).map(md => [md.id, "waiting"]))
    )

    // Sincronizar dayResponses sempre que event mudar
    useEffect(() => {
        if (!event) return;
        // Tenta encontrar o participante atual
        const myResponses: Record<number, "accepted" | "declined" | "waiting"> = {};
        (event.matchDaySessions || []).forEach(md => {
            // Procura o participante deste dia
            const p = (event.participants || []).find(p => p.userId === participantId && p.matchDayId === md.id);
            const status = p?.confirmationStatus || p?.status;
            if (p) {
                myResponses[md.id] = status === "accepted" ? "accepted" : status === "declined" ? "declined" : "waiting";
            } else {
                myResponses[md.id] = "waiting";
            }
        });
        setDayResponses(myResponses);
    }, [event, participantId]);

    if (!event) return <div className="error-message">Erro: convocatória não encontrada.</div>

    // Agrupamento seguro de participantes
    const groupedParticipants: Record<
        string,
        {
            category: string
            days: { date: string; func: string; status: string }[]
        }
    > = {}

    if (Array.isArray(event.participants)) {
        event.participants.forEach((p: ParticipantInfo) => {
            const matchDay = (event.matchDaySessions || []).find((md) => md.id === p.matchDayId)
            if (!matchDay) return
            const name = p.name || (p as any).userName || (p as any).email || String(p.userId) || "Participante"
            if (!groupedParticipants[name]) {
                groupedParticipants[name] = { category: p.category, days: [] }
            }
            groupedParticipants[name].days.push({
                date: new Date(matchDay.matchDate).toLocaleDateString(),
                func: p.function,
                status: p.confirmationStatus || p.status // inclui status
            })
        })
    }


    const updateDayResponse = (dayId: number, response: "accepted" | "declined") => {
        setDayResponses((prev) => ({
            ...prev,
            [dayId]: prev[dayId] === response ? "waiting" : response
        }))
    }

    const fetchEvent = async () => {
        try {
            const token = getCookie("token");
            const response = await fetch(`/arbnet/callList/get/${event.callListId}`, {
                headers: {
                    Authorization: `bearer ${getCookie("token")}`,
                }
            });
            if (!response.ok) throw new Error("Erro ao buscar convocatória atualizada");
            const data = await response.json();
            setEvent(data);
            // Atualizar dayResponses imediatamente após atualizar o evento
            const myResponses: Record<number, "accepted" | "declined" | "waiting"> = {};
            (data.matchDaySessions || []).forEach((md: any) => {
                const p = (data.participants || []).find((p: any) => p.userId === participantId && p.matchDayId === md.id);
                const status = p?.confirmationStatus || p?.status;
                if (p) {
                    myResponses[md.id] = status === "accepted" ? "accepted" : status === "declined" ? "declined" : "waiting";
                } else {
                    myResponses[md.id] = "waiting";
                }
            });
            setDayResponses(myResponses);
        } catch (err) {
            alert("Erro ao atualizar dados da convocatória.");
        }
    };

    const handleSubmit = () => {
        const token = getCookie("token");
        if (!token) {
            alert("Token não encontrado. Faça login novamente.");
            return;
        }
        // Montar a lista de confirmações para cada dia
        const days: MatchDayConfirmation[] = Object.entries(dayResponses)
            .filter(([_, value]) => value === "accepted" || value === "declined")
            .map(([key, value]) => ({
                dayId: Number(key),
                status: value === "accepted" ? 1 : 0
            }));
        const input: ParticipantUpdateInput = {
            days,
            callListId: event.callListId
        };
        fetch("/arbnet/callList/updateParticipant", {
            method: "PUT",
            headers: {
                "Content-Type": "application/json",
                Authorization: `bearer ${getCookie("token")}`,
            },
            body: JSON.stringify(input)
        })
            .then((res) => {
                if (!res.ok) throw new Error("Erro ao atualizar confirmação")
                alert("Confirmação atualizada com sucesso!")
                fetchEvent();
            })
            .catch((err) => {
                console.error(err)
                alert("Erro ao enviar confirmação.")
            })
    }

    const currentRole = useCurrentRole();

    const handleCouncilSubmit = () => {
        const token = getCookie("token");
        if (!token) {
            alert("Token não encontrado. Faça login novamente.");
            return;
        }

        const days: MatchDayConfirmation[] = Object.entries(dayResponses)
            .filter(([_, value]) => value === "accepted")
            .map(([key]) => ({
                dayId: Number(key),
                status: 1
            }));

        const input: ParticipantUpdateInput = {
            days,
            callListId: event.callListId
        }

        fetch("/arbnet/callList/updateParticipant/ArbitrationCouncil", {
            method: "PUT",
            headers: {
                "Content-Type": "application/json",
                Authorization: `bearer ${getCookie("token")}`,
            },
            body: JSON.stringify(input)
        })
            .then((res) => {
                if (!res.ok) throw new Error("Erro ao atualizar confirmação pelo conselho de arbitragem")
                alert("Confirmação do participante atualizada pelo conselho de arbitragem com sucesso!")
            })
            .catch((err) => {
                console.error(err)
                alert("Erro ao enviar confirmação pelo conselho de arbitragem.")
            })
    }

    return (
        <div className="calllist-info-container">
            {/* Info da convocatória */}
            <h2>{event.competitionName}</h2>
            <div className="calllist-details">
                <p><strong>Morada:</strong> {event.address}</p>
                <p><strong>Local:</strong> {event.location}</p>
                <p><strong>Associação:</strong> {event.association}</p>
                <p><strong>Contactos:</strong> {event.phoneNumber} / {event.email}</p>
                <p><strong>Data limite:</strong> {new Date(event.deadline).toLocaleDateString()}</p>
            </div>

            {/* Participantes */}
            <hr className="section-divider" />
            <h3>Participantes Convocados</h3>
            {Object.entries(groupedParticipants).map(([name, { category, days }]) => (
                <div key={name} className="participant-block">
                    <strong>{name}</strong> <em>({category})</em>
                    <ul>
                        {days.map((day, i) => (
                            <li key={i}>
                                {day.date} — <em>{day.func}</em> —{" "}
                                <strong
                                    className={
                                        day.status === "accepted"
                                            ? "participant-status-accepted"
                                            : day.status === "declined"
                                                ? "participant-status-declined"
                                                : "participant-status-waiting"
                                    }
                                >
                                    {day.status === "accepted"
                                        ? "Aceitou"
                                        : day.status === "declined"
                                            ? "Recusou"
                                            : "À espera"}
                                </strong>
                            </li>
                        ))}
                    </ul>
                </div>
            ))}


            {/* Equipamentos */}
            <hr className="section-divider"/>
            <h3>Equipamentos Associados</h3>
            <ul className="equipment-list">
                {event.equipments.map((e) => (
                    <li key={e.id}>{e.name}</li>
                ))}
            </ul>

            {/* Confirmação de dias (visível apenas se o tipo permitir) */}
            {["sealedCallList", "finalJury"].includes(event.callListType) && (
                <div className="confirmation-section">
                    <hr className="section-divider" />
                    <h3>Confirmação de Presença</h3>
                    <ul>
                        {event.matchDaySessions.map((matchDay) => (
                            <li key={matchDay.id} className="confirmation-item">
                                <div className="confirmation-item-header">
                                    <strong>{new Date(matchDay.matchDate).toLocaleDateString()}</strong>
                                    <div className="confirmation-item-sessions">
                                    {matchDay.sessions.length > 0 ? (
                                        matchDay.sessions.map(session => (
                                            <span key={session.id} className="confirmation-item-session">
                                                {session.startTime.slice(0, 5)}
                                            </span>
                                        ))
                                    ) : (
                                        <em>Sem sessões</em>
                                    )}
                                    </div>
                                </div>

                                <div className="confirmation-options">
                                    <label>
                                        <input
                                            type="checkbox"
                                            checked={dayResponses[matchDay.id] === "accepted"}
                                            onChange={() => updateDayResponse(matchDay.id, "accepted")}
                                        />
                                        ✔️ Aceitar
                                    </label>

                                    <label>
                                        <input
                                            type="checkbox"
                                            checked={dayResponses[matchDay.id] === "declined"}
                                            onChange={() => updateDayResponse(matchDay.id, "declined")}
                                        />
                                        ❌ Recusar
                                    </label>
                                </div>
                            </li>
                        ))}
                    </ul>

                    <button onClick={handleSubmit} className="submit-confirmation-btn">Submeter Confirmação</button>
                </div>
            )}
        </div>
    )
}
