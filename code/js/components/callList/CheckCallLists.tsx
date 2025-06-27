// src/components/callList/CheckCallLists.tsx
import React, { useEffect, useState } from "react"
import { useCurrentUser } from "../../src/context/Authn"
import { useNavigate } from "react-router-dom"

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

export function CheckCallLists() {
    const currentUser = useCurrentUser()
    const [events, setEvents] = useState<RefereeCallListsOutputModel[]>([])
    const [loading, setLoading] = useState(true)
    const navigate = useNavigate()

    useEffect(() => {
        if (!currentUser) return

        fetch(`/arbnet/callList/referee/13`)
            .then(res => {
                if (!res.ok) throw new Error("Erro ao obter convocações")
                return res.json()
            })
            .then((data: RefereeCallListsOutputModel[]) => setEvents(data))
            .catch(err => console.error(err))
            .finally(() => setLoading(false))
    }, [currentUser])

    if (loading) return <div>A carregar convocações...</div>

    return (
        <div>
            <h2>As Minhas Convocações</h2>
            {events.length === 0 ? (
                <p>Não tens convocações.</p>
            ) : events.map((event, index) => (
                <div key={index} className="calllist-card">
                    <h3>{event.competitionName}</h3>
                    <p><strong>Data Limite:</strong> {new Date(event.deadline).toLocaleDateString()}</p>

                    <strong>Dias da Convocatória:</strong>
                    <ul>
                        {event.matchDaySessions.map((mdf, i) => {
                            const participant = event.participants.find(p =>
                                p.matchDayId === mdf.id &&
                                p.userId === 13 // currentUser?.id
                            )

                            return (
                                <li key={i}>
                                    {new Date(mdf.matchDate).toLocaleDateString()} — {participant?.function ?? "Sem função"}
                                </li>
                            )
                        })}
                    </ul>

                    <button onClick={() => navigate("/callList-info", { state: { event } })}>
                        Mais informação
                    </button>
                </div>
            ))}
        </div>
    )
}



