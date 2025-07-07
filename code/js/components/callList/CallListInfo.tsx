import React, { useState } from "react"
import { useLocation } from "react-router-dom"
import "../../CallListInfo.css"
import {getCookie} from "./CreateCallList";

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

interface ParticipantUpdateInput {
    days: number[]
    participantId: number
    callListId: number
}

export function CallListInfo() {
    const location = useLocation()
    const event = location.state?.event as RefereeCallListsOutputModel
    const participantId = location.state?.participantId as number

    const [dayResponses, setDayResponses] = useState<Record<number, "accepted" | "declined" | "waiting">>(
        () => Object.fromEntries(event.matchDaySessions.map(md => [md.id, "waiting"]))
    )

    if (!event) return <div className="error-message">Erro: convocatória não encontrada.</div>

    const groupedParticipants: Record<
        string,
        {
            category: string
            days: { date: string; func: string; status: string }[]
        }
    > = {}

    event.participants.forEach((p) => {
        const matchDay = event.matchDaySessions.find((md) => md.id === p.matchDayId)
        if (!matchDay) return

        if (!groupedParticipants[p.name]) {
            groupedParticipants[p.name] = { category: p.category, days: [] }
        }

        groupedParticipants[p.name].days.push({
            date: new Date(matchDay.matchDate).toLocaleDateString(),
            func: p.function,
            status: p.status // inclui status
        })
    })


    const updateDayResponse = (dayId: number, response: "accepted" | "declined") => {
        setDayResponses((prev) => ({
            ...prev,
            [dayId]: prev[dayId] === response ? "waiting" : response
        }))
    }

    const handleSubmit = () => {

        const token = getCookie("token");
        if (!token) {
            alert("Token não encontrado. Faça login novamente.");
            return;
        }

        const acceptedDays = Object.entries(dayResponses)
            .filter(([_, value]) => value === "accepted")
            .map(([key]) => Number(key))

        const input: ParticipantUpdateInput = {
            days: acceptedDays,
            participantId: participantId,
            callListId: event.callListId
        }

        fetch("/arbnet/callList/updateParticipant", {
            method: "PUT",
            headers: {
                "Content-Type": "application/json",
                token
            },
            body: JSON.stringify(input)
        })
            .then((res) => {
                if (!res.ok) throw new Error("Erro ao atualizar confirmação")
                alert("Confirmação atualizada com sucesso!")
            })
            .catch((err) => {
                console.error(err)
                alert("Erro ao enviar confirmação.")
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
