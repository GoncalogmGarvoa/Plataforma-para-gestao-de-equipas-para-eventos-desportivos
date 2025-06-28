import * as React from 'react'
import { Navigate, useLocation } from 'react-router-dom'
import {useCurrentUser, useSetUser} from './context/Authn'
import {useSetEmail, useSetRole} from "./context/Referee";

export function RequireAuthn({ children }: { children: React.ReactNode }): React.ReactElement {
    const currentUser = useCurrentUser()
    const location = useLocation()
    const setUser = useSetUser()
    const setEmail = useSetEmail()
    const setRole = useSetRole()
    console.log(`currentUser = ${currentUser}`)


    if (currentUser) {
        const token = getCookie('token')
        const email = getCookie('email')
        const role = getCookie('role')

        if (token && email) {
            setUser(token + "=")
            setEmail(email)
            if (role) {
                setRole(role)
            }
        }

        return <>{children}</>

    } else {
        console.log("redirecting to login")
        return <Navigate to="/login" state={{source: location.pathname}} replace={true}/>
    }

}

function getCookie(name: string): string | null {
    // @ts-ignore
    const match = document.cookie.split('; ').find(row => row.startsWith(`${name}=`))
    return match ? match.split('=')[1] : null
}