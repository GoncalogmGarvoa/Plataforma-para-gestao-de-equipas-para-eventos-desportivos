import React, { useState } from "react"
import { useLocation } from "react-router-dom"

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

    if (!event) return <div>Erro: convocatória não encontrada.</div>

    // Agrupar participantes por nome
    const groupedParticipants: Record<string, { category: string; days: { date: string; func: string }[] }> = {}
    event.participants.forEach((p) => {
        const matchDay = event.matchDaySessions.find((md) => md.id === p.matchDayId)
        if (!matchDay) return

        if (!groupedParticipants[p.name]) {
            groupedParticipants[p.name] = { category: p.category, days: [] }
        }

        groupedParticipants[p.name].days.push({
            date: new Date(matchDay.matchDate).toLocaleDateString(),
            func: p.function
        })
    })

    const updateDayResponse = (dayId: number, response: "accepted" | "declined") => {
        setDayResponses((prev) => ({
            ...prev,
            [dayId]: prev[dayId] === response ? "waiting" : response
        }))
    }

    const handleSubmit = () => {
        const acceptedDays = Object.entries(dayResponses)
            .filter(([_, value]) => value === "accepted")
            .map(([key]) => Number(key))

        const input: ParticipantUpdateInput = {
            days: acceptedDays,
            participantId: participantId ?? 13,
            callListId: event.callListId
        }

        fetch("/arbnet/callList/updateParticipant", {
            method: "PUT",
            headers: {
                "Content-Type": "application/json"
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
        <div className="calllist-info">
            {/* Info da convocatória */}
            <h2>{event.competitionName}</h2>
            <p><strong>Morada:</strong> {event.address}</p>
            <p><strong>Local:</strong> {event.location}</p>
            <p><strong>Associação:</strong> {event.association}</p>
            <p><strong>Contactos:</strong> {event.phoneNumber} / {event.email}</p>
            <p><strong>Data limite:</strong> {new Date(event.deadline).toLocaleDateString()}</p>

            {/* Participantes */}
            <hr />
            <h3>Participantes Convocados</h3>
            {Object.entries(groupedParticipants).map(([name, { category, days }]) => (
                <div key={name} className="participant-block">
                    <strong>{name}</strong> <em>({category})</em>
                    <ul>
                        {days.map((day, i) => (
                            <li key={i}>{day.date} — <em>{day.func}</em></li>
                        ))}
                    </ul>
                </div>
            ))}

            {/* Equipamentos */}
            <hr />
            <h3>Equipamentos Associados</h3>
            <ul>
                {event.equipments.map((e) => (
                    <li key={e.id}>{e.name}</li>
                ))}
            </ul>

            {/* Confirmação de dias */}
            <hr />
            <h3>Confirmação de Presença</h3>
            <ul>
                {event.matchDaySessions.map((matchDay) => (
                    <li key={matchDay.id} style={{ marginBottom: "1em" }}>
                        <div>
                            <strong>{new Date(matchDay.matchDate).toLocaleDateString()}</strong>
                            {" — "}
                            {matchDay.sessions.length > 0 ? (
                                matchDay.sessions.map(session => (
                                    <span key={session.id} style={{ marginRight: "0.5em" }}>
                                        {session.startTime.slice(0, 5)}/
                                    </span>
                                ))
                            ) : (
                                <em>Sem sessões</em>
                            )}
                        </div>

                        <label>
                            <input
                                type="checkbox"
                                checked={dayResponses[matchDay.id] === "accepted"}
                                onChange={() => updateDayResponse(matchDay.id, "accepted")}
                            />
                            ✔️ Aceitar
                        </label>

                        <label style={{ marginLeft: "1em" }}>
                            <input
                                type="checkbox"
                                checked={dayResponses[matchDay.id] === "declined"}
                                onChange={() => updateDayResponse(matchDay.id, "declined")}
                            />
                            ❌ Recusar
                        </label>
                    </li>
                ))}
            </ul>

            <button onClick={handleSubmit}>Submeter Confirmação</button>
        </div>
    )
}
