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

    useEffect(() => {
        const fetchUser = async () => {
            try {
                const response = await fetch(`/arbnet/users/email?email=${encodeURIComponent(email!)}`)

                if (!response.ok) {
                    throw new Error("User not Found.")
                }

                const data = await response.json()
                setUser(data)
            } catch (error) {
                console.error("User Not Found:", error)
            }
        }

        if (email) {
            fetchUser()
        }
    }, [email])

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
