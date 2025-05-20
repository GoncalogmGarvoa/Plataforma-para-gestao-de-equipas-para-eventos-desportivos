import * as React from 'react'
import {
    useState,
    createContext,
    useContext,
} from 'react'

type ContextType = {
    email: string | undefined,
    setEmail: (v: string | undefined) => void
}
const EmailInContext = createContext<ContextType>({
    email: undefined,
    setEmail: () => { },
})



// @ts-ignore
const getEmailFromCookie = (): string | undefined => {
    const cookie = document.cookie.split('; ').find(row => row.startsWith('email='))
    return cookie ? cookie.split('=')[1] : undefined
}

export function UserContainer({ children }: { children: React.ReactNode }) {
    const [user, setUser] = useState(getEmailFromCookie)

    return (
        <EmailInContext.Provider value={{ email: user, setEmail: setUser }}>
            {children}
        </EmailInContext.Provider>
    )
}

export function useCurrentEmail() {
    return useContext(EmailInContext).email
}

export function useSetEmail() {
    return useContext(EmailInContext).setEmail
}