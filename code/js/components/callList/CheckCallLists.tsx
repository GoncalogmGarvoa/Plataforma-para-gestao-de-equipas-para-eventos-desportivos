// src/components/callList/CheckCallLists.tsx
import React, { useEffect, useState } from "react"
import { useNavigate } from "react-router-dom"
import { getCookie } from "./CreateCallList"
import "../../CheckCallLists.css"

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

export function CheckCallLists() {
    const [events, setEvents] = useState<RefereeCallListsOutputModel[]>([])
    const [loading, setLoading] = useState(true)
    const [userId, setUserId] = useState<number | null>(null)
    const [showPast, setShowPast] = useState(false)
    const navigate = useNavigate()

    useEffect(() => {
        const token = getCookie("token")
        if (!token) return

        const fetchData = async () => {
            try {

                // Buscar convocatórias
                const resEvents = await fetch("/arbnet/callList/referee", {
                    method: "GET",
                    headers: { token }
                })

                if (!resEvents.ok) throw new Error("Erro ao obter convocações")
                const data: RefereeCallListsOutputModel[] = await resEvents.json()
                setEvents(data)
            } catch (err) {
                console.error(err)
            } finally {
                setLoading(false)
            }
        }

        fetchData()
    }, [])

    const today = new Date().toISOString().split("T")[0]

    const filteredEvents = events.filter(event => {
        const dates = event.matchDaySessions.map(md => md.matchDate)
        const latestDate = dates.sort().at(-1)
        if (!latestDate) return false
        return showPast ? latestDate < today : latestDate >= today
    })

    if (loading) return <div>A carregar convocatórias...</div>

    return (
        <div className="check-call-lists-container">
            <h2>As Minhas Convocações</h2>

            <div style={{ display: "flex", justifyContent: "flex-end", marginBottom: "1rem" }}>
                <button onClick={() => setShowPast(prev => !prev)}>
                    {showPast ? "Ver Convocatórias Atuais" : "Ver Convocatórias Antigas"}
                </button>
            </div>

            {filteredEvents.length === 0 ? (
                <p className="no-events-message">Não tens convocatórias.</p>
            ) : filteredEvents.map((event, index) => (
                // restante código

                <div key={index} className="calllist-card">
                    <h3>{event.competitionName}</h3>
                    <p><strong>Data Limite:</strong> {new Date(event.deadline).toLocaleDateString()}</p>

                    <strong>Dias da Convocatória:</strong>
                    <ul>
                        {event.matchDaySessions.map((mdf, i) => {
                            const participant = event.participants.find(p =>
                                p.matchDayId === mdf.id
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
