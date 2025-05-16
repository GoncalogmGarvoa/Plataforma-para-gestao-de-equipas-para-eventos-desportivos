import * as React from 'react'
import { Navigate, useLocation } from 'react-router-dom'
import {useCurrentUser, useSetUser} from './context/Authn'
import {useSetUsername} from "./context/Player";

export function RequireAuthn({ children }: { children: React.ReactNode }): React.ReactElement {
    const currentUser = useCurrentUser()
    const location = useLocation()
    const setUser = useSetUser()
    const setUsername = useSetUsername()
    console.log(`currentUser = ${currentUser}`)

    /*
    const getCookie = async () => {
        try {
            const response = await fetch("/api/cookies")
            const data = await response.json()
            console.log("Cookies: ", data)
            if (response.ok) {
                return data
            } else {
                return undefined
            }
        }
        catch (error) {
            console.error("Empty cookies:", error)
            throw error
        }
    }

    if (currentUser == undefined) {
        getCookie().then((cookie => {
            if (cookie != undefined) {
                setUser(cookie.token)
                setUsername(cookie.username)
            }
        }))
        return <>{children}</>
    }
     */
    if (currentUser) {
        const token = getCookie('token')
        const username = getCookie('username')

        if (token && username) {
            setUser(token + "=")
            setUsername(username)
        }

        return <>{children}</>

        /*setUser(document.cookie.split('; ').find(row => row.startsWith('token=')).split('=')[1]+"=")
        setUsername(document.cookie.split('; ').find(row => row.startsWith('username=')).split('=')[1])
        return <>{children}</>
         */
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
