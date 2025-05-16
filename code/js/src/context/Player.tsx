import * as React from 'react'
import {
    useState,
    createContext,
    useContext,
} from 'react'

type ContextType = {
    username: string | undefined,
    setUsername: (v: string | undefined) => void
}
const UsernameInContext = createContext<ContextType>({
    username: undefined,
    setUsername: () => { },
})

/*
const getUserNameFromCookie = async () => {
    try {
        const response = await fetch("/api/cookies")
        const data = await response.json()
        console.log("Cookies: ", data)
        if (response.ok) {
            return data.username
        } else {
            return undefined
        }
    }
    catch (error) {
        console.error("Empty cookies:", error)
        throw error
    }
}
 */

// @ts-ignore
const getUserNameFromCookie = () => document.cookie.split('; ').find(row => row.startsWith('username='))==undefined ? document.cookie.split('; ').find(row => row.startsWith('username=')) : document.cookie.split('; ').find(row => row.startsWith('username=')).split('=')[1]

export function UserContainer({ children }: { children: React.ReactNode }) {
    const [user, setUser] = useState(getUserNameFromCookie)
    /*
    useEffect(() => {
        getUserNameFromCookie().then((cookie => {
            if (cookie != user) {
                setUser(cookie)
            }
        }))
    }, [user]);
     */
    return (
        <UsernameInContext.Provider value={{ username: user, setUsername: setUser }}>
            {children}
        </UsernameInContext.Provider>
    )
}

export function useCurrentUsername() {
    return useContext(UsernameInContext).username
}

export function useSetUsername() {
    return useContext(UsernameInContext).setUsername
}