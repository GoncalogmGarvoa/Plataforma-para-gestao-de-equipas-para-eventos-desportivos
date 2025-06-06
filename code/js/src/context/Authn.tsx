import * as React from 'react'
import {
    useState,
    createContext,
    useContext,
} from 'react'

type ContextType = {
    user: string | undefined,
    setUser: (v: string | undefined) => void
}
const LoggedInContext = createContext<ContextType>({
    user: undefined,
    setUser: () => { },
})

// @ts-ignore
const getTokenFromCookie = () => document.cookie.split('; ').find(row => row.startsWith('token='))==undefined ? document.cookie.split('; ').find(row => row.startsWith('token=')) : document.cookie.split('; ').find(row => row.startsWith('token=')).split('=')[1]+"="



export function AuthnContainer({ children }: { children: React.ReactNode }) {
    //const [user, setUser] = useState(undefined)
    const [user, setUser] = useState(getTokenFromCookie())

    return (
        <LoggedInContext.Provider value={{ user: user, setUser: setUser }}>
            {children}
        </LoggedInContext.Provider>
    )
}

export function useCurrentUser() {
    return useContext(LoggedInContext).user
}

export function useSetUser() {
    return useContext(LoggedInContext).setUser
}