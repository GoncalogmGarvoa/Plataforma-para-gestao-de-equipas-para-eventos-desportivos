import {useEffect, useState} from "react"
import * as React from "react"

import { useCurrentEmail } from "../../src/context/Player"


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

    useEffect(() => {
        const fetchUser = async () => {
            try {
                const response = await fetch(`/arbnet/users/email?email=${encodeURIComponent(email!)}`)

                const data = await response.json()

                if (!response.ok) {
                    setError(data.title || "Error getting user.")
                    return
                }

                setUser(data)
            } catch (error: any) {
                console.error("Error getting user:", error)
                setError(error.message)
            }
        }

        if (email) {
            fetchUser()
        }
    }, [email])

    if (error) return (
        <div className="center-container">
            <div className="form-container">
                <p className="error-message">Error: {error}</p>
            </div>
        </div>
    )
    
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
                <h1 className="form-title">User Profile</h1>
                <div className="profile-info">
                    <div className="profile-item">
                        <span className="profile-label">Name</span>
                        <span className="profile-value">{user.name}</span>
                    </div>
                    <div className="profile-item">
                        <span className="profile-label">Email</span>
                        <span className="profile-value">{user.email}</span>
                    </div>
                    <div className="profile-item">
                        <span className="profile-label">Phone Number</span>
                        <span className="profile-value">{user.phoneNumber}</span>
                    </div>
                    <div className="profile-item">
                        <span className="profile-label">Address</span>
                        <span className="profile-value">{user.address}</span>
                    </div>
                    <div className="profile-item">
                        <span className="profile-label">Birth Date</span>
                        <span className="profile-value">{new Date(user.birthDate).toLocaleDateString()}</span>
                    </div>
                    <div className="profile-item">
                        <span className="profile-label">IBAN</span>
                        <span className="profile-value">{user.iban}</span>
                    </div>
                    <div className="profile-item">
                        <span className="profile-label">Roles</span>
                        <span className="profile-value">
                            {user.roles.map((role, index) => (
                                <span key={index} className="role-badge">
                                    {role}
                                </span>
                            ))}
                        </span>
                    </div>
                </div>
            </div>
        </div>
    )
}

