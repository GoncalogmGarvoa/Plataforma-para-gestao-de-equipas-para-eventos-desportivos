// src/components/callList/CheckCallLists.tsx
import React, { useEffect, useState } from "react"
import { useNavigate } from "react-router-dom"
import { getCookie } from "../../src/context/Authn"
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

interface PaginatedResponse {
    content: RefereeCallListsOutputModel[]
    page: number
    size: number
    totalPages: number
    totalElements: number
}

export function CheckCallLists() {
    const [events, setEvents] = useState<RefereeCallListsOutputModel[]>([])
    const [loading, setLoading] = useState(true)
    const [page, setPage] = useState(0)
    const [size] = useState(5) // Pode ajustar conforme o desejado
    const [totalPages, setTotalPages] = useState(1)
    const [showPast, setShowPast] = useState(false)
    const navigate = useNavigate()

    useEffect(() => {
        const token = getCookie("token")
        if (!token) return

        const fetchData = async () => {
            setLoading(true)
            try {
                const resEvents = await fetch(`/arbnet/callList/referee?page=${page}&size=${size}`, {
                    method: "GET",
                    headers: {
                        Authorization: `bearer ${token}`,
                    }
                })

                if (!resEvents.ok) throw new Error("Erro ao obter convocações")

                const data: PaginatedResponse = await resEvents.json()
                setEvents(data.content)
                setTotalPages(data.totalPages)
            } catch (err) {
                console.error(err)
            } finally {
                setLoading(false)
            }
        }

        fetchData()
    }, [page])

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
            <h2>As Minhas Convocatórias</h2>

            <div style={{ display: "flex", justifyContent: "space-between", marginBottom: "1rem" }}>
                <button onClick={() => setShowPast(prev => !prev)}>
                    {showPast ? "Ver Convocatórias Atuais" : "Ver Convocatórias Antigas"}
                </button>
                <div className="pagination-controls">
                    <button
                        disabled={page === 0}
                        onClick={() => setPage(prev => prev - 1)}
                    >
                        Anterior
                    </button>
                    <span style={{ margin: "0 1rem" }}>Página {page + 1} de {totalPages}</span>
                    <button
                        disabled={page + 1 >= totalPages}
                        onClick={() => setPage(prev => prev + 1)}
                    >
                        Próxima
                    </button>
                </div>
            </div>

            {filteredEvents.length === 0 ? (
                <p className="no-events-message">Não tens convocatórias.</p>
            ) : filteredEvents.map((event, index) => (
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
