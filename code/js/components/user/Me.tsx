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

    if (error) return <p style={{ color: "red" }}>Erro: {error}</p>
    if (!user) return <p>Loading User Info...</p>

    return (
        <div>
            <h2>User Profile</h2>
            <ul>
                <li><strong>Name:</strong> {user.name}</li>
                <li><strong>Email:</strong> {user.email}</li>
                <li><strong>PhoneNumber:</strong> {user.phoneNumber}</li>
                <li><strong>Address:</strong> {user.address}</li>
                <li><strong>BirthDate:</strong> {user.birthDate}</li>
                <li><strong>Iban:</strong> {user.iban}</li>
                <li><strong>Roles:</strong> {user.roles.join(", ")}</li>
            </ul>
        </div>
    )
}

