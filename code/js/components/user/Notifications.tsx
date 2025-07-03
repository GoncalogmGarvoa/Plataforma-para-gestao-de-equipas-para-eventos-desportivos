import { useEffect, useState } from "react"
import { FaBell } from "react-icons/fa"
import { useCurrentRole } from "../../src/context/Referee"
import React from "react"

type Notification = {
    id: number
    userId: number
    roleId: number
    message: string
    createdAt: string
    readStatus: boolean
}

type Role = {
    id: number
    name: string
}

export function Notifications() {
    const [notifications, setNotifications] = useState<Notification[]>([])
    const [roles, setRoles] = useState<Role[]>([])
    const [showDropdown, setShowDropdown] = useState(false)

    const currentRole = useCurrentRole()
    const hardcodedUserId = 13 // ← usar isto por agora

    useEffect(() => {
        fetch("/arbnet/users/roles")
            .then(res => res.json())
            .then(data => setRoles(data))
            .catch(err => {
                console.error("Erro ao obter roles", err)
                alert("Erro ao carregar roles")
            })
    }, [])

    useEffect(() => {
        if (!currentRole || roles.length === 0) return

        const roleObj = roles.find(r => r.name === currentRole)
        if (!roleObj) return

        fetch(`/arbnet/users/notifications?userId=${hardcodedUserId}&roleId=${roleObj.id}`)
            .then(res => res.json())
            .then(data => {
                const unread = data.filter((n: Notification) => !n.readStatus)
                setNotifications(unread)
            })
            .catch(err => {
                console.error("Erro ao obter notificações", err)
                alert("Erro ao carregar notificações")
            })
    }, [currentRole, roles])

    const markAsRead = (notificationId: number) => {
        fetch(`/arbnet/users/notifications/read/${notificationId}`, {
            method: "PUT"
        })
            .then(() => {
                setNotifications(prev => prev.filter(n => n.id !== notificationId))
            })
            .catch(err => {
                console.error("Erro ao marcar notificação como lida", err)
                alert("Erro ao marcar notificação como lida")
            })
    }

    return (
        <div style={{ position: "relative" }}>
            <button
                onClick={() => setShowDropdown(!showDropdown)}
                style={{
                    background: "none",
                    border: "none",
                    cursor: "pointer",
                    fontSize: "20px",
                    color: "#333"
                }}
                title="Notificações"
            >
                <FaBell />
                {notifications.length > 0 && (
                    <span className="notification-badge">{notifications.length}</span>
                )}
            </button>

            {showDropdown && notifications.length > 0 && (
                <ul className="notification-dropdown">
                    {notifications.map(n => (
                        <li key={n.id}>
                            {n.message}
                            <button onClick={() => markAsRead(n.id)}>Marcar como lida</button>
                        </li>
                    ))}
                </ul>
            )}
        </div>
    )
}
