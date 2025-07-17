import {useEffect, useState, useCallback} from "react"
import * as React from "react"

import { useCurrentEmail } from "../../src/context/Referee"


export function Me() {
    const email = useCurrentEmail()
    const [user, setUser] = useState<null | {
        id: string,
        name: string,
        email: string,
        phoneNumber: string,
        address: string,
        birthDate: string,
        iban: string,
        roles: string[]
    }>(null)

    const [error, setError] = useState<string | undefined>(undefined)
    const [isEditing, setIsEditing] = useState(false)
    const [editName, setEditName] = useState("")
    const [editPhoneNumber, setEditPhoneNumber] = useState("")
    const [editAddress, setEditAddress] = useState("")
    const [editBirthDate, setEditBirthDate] = useState("")
    const [editIban, setEditIban] = useState("")
    const [editEmail, setEditEmail] = useState("")

    const fetchUser = useCallback(async () => {
        try {
            const response = await fetch(`/arbnet/users/email?email=${encodeURIComponent(email!)}`)

            const data = await response.json()

            if (!response.ok) {
                setError(data.title || "Error getting user.")
                return
            }

            setUser(data)
            setEditName(data.name)
            setEditPhoneNumber(data.phoneNumber)
            setEditAddress(data.address)
            setEditBirthDate(data.birthDate.split('T')[0]) // Format for input type="date"
            setEditIban(data.iban)
            setEditEmail(data.email)
        } catch (error: any) {
            console.error("Error getting user:", error)
            setError(error.message)
        }
    }, [email])

    useEffect(() => {
        if (email) {
            fetchUser()
        }
    }, [email, fetchUser])

    const handleSave = async () => {
        setError(undefined) // Clear previous errors
        try {
            const response = await fetch("/arbnet/users/update", {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({
                    id: user!.id,
                    name: editName,
                    phoneNumber: editPhoneNumber,
                    address: editAddress,
                    email: editEmail,
                    password: "", // Password not editable through this form, send empty string or handle separately
                    birthDate: editBirthDate + "T00:00:00Z", // Convert back to ISO string for backend
                    iban: editIban,
                }),
            })

            const data = await response.json()

            if (!response.ok) {
                setError(data.title || "Error updating user.")
                return
            }

            // Re-fetch user data to ensure all fields are updated
            await fetchUser() // Call fetchUser directly
            setIsEditing(false) // Exit edit mode
            alert("Profile updated successfully!")
        } catch (error: any) {
            console.error("Error updating user:", error)
            setError(error.message)
        }
    }

    if (!user) return (
        <div className="center-container">
            <div className="form-container">
                <p className="form-title">Loading User Info...</p>
            </div>
        </div>
    )

    return (
        <div className="center-container">
            <div className="form-container">
                <h1 className="form-title">Informação do Utilizador</h1>
                {error && <p className="error-message">Error: {error}</p>}
                {isEditing ? (
                    <div className="profile-edit-form">
                        <div className="profile-item">
                            <label className="profile-label">Nome:</label>
                            <input
                                type="text"
                                className="profile-input"
                                value={editName}
                                onChange={(e) => setEditName(e.target.value)}
                            />
                        </div>
                        <div className="profile-item">
                            <label className="profile-label">Email:</label>
                            <input
                                type="email"
                                className="profile-input"
                                value={editEmail}
                                onChange={(e) => setEditEmail(e.target.value)}
                            />
                        </div>
                        <div className="profile-item">
                            <label className="profile-label">Número de Telemóvel:</label>
                            <input
                                type="text"
                                className="profile-input"
                                value={editPhoneNumber}
                                onChange={(e) => setEditPhoneNumber(e.target.value)}
                            />
                        </div>
                        <div className="profile-item">
                            <label className="profile-label">Morada:</label>
                            <input
                                type="text"
                                className="profile-input"
                                value={editAddress}
                                onChange={(e) => setEditAddress(e.target.value)}
                            />
                        </div>
                        <div className="profile-item">
                            <label className="profile-label">Data de Nascimento:</label>
                            <input
                                type="date"
                                className="profile-input"
                                value={editBirthDate}
                                onChange={(e) => setEditBirthDate(e.target.value)}
                            />
                        </div>
                        <div className="profile-item">
                            <label className="profile-label">IBAN:</label>
                            <input
                                type="text"
                                className="profile-input"
                                value={editIban}
                                onChange={(e) => setEditIban(e.target.value)}
                            />
                        </div>
                        <div className="profile-actions">
                            <button className="button" onClick={handleSave}>Save</button>
                            <button className="button cancel-button" onClick={() => setIsEditing(false)}>Cancel</button>
                        </div>
                    </div>
                ) : (
                    <div className="profile-info">
                        <div className="profile-item">
                            <span className="profile-label">Nome</span>
                            <span className="profile-value">{user.name}</span>
                        </div>
                        <div className="profile-item">
                            <span className="profile-label">Email</span>
                            <span className="profile-value">{user.email}</span>
                        </div>
                        <div className="profile-item">
                            <span className="profile-label">Número de Telemóvel</span>
                            <span className="profile-value">{user.phoneNumber}</span>
                        </div>
                        <div className="profile-item">
                            <span className="profile-label">Morada</span>
                            <span className="profile-value">{user.address}</span>
                        </div>
                        <div className="profile-item">
                            <span className="profile-label">Data de Nascimento</span>
                            <span className="profile-value">{new Date(user.birthDate).toLocaleDateString()}</span>
                        </div>
                        <div className="profile-item">
                            <span className="profile-label">IBAN</span>
                            <span className="profile-value">{user.iban}</span>
                        </div>
                        <div className="profile-item">
                            <span className="profile-label">Perfis</span>
                            <span className="profile-value">
                                {(user.roles || []).map((role, index) => (
                                    <span key={index} className="role-badge">
                                        {role}
                                    </span>
                                ))}
                            </span>
                        </div>
                        <button className="button" onClick={() => setIsEditing(true)}>Editar Informação</button>
                    </div>
                )}
            </div>
        </div>
    )
}

