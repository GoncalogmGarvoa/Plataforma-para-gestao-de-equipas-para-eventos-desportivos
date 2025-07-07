import * as React from 'react'
import {
    useState,
    createContext,
    useContext,
} from 'react'

type ContextType = {
    email: string | undefined,
    setEmail: (v: string | undefined) => void,
    role: string | undefined,
    setRole: (v: string | undefined) => void,
}

const InfoInContext = createContext<ContextType>({
    email: undefined,
    setEmail: () => {},
    role: undefined,
    setRole: () => {},
})

const getCookie = (name: string): string | undefined => {
    const match = document.cookie.split('; ').find(row => row.startsWith(`${name}=`));
    return match ? match.split('=')[1] : undefined;
};

export function UserContainer({ children }: { children: React.ReactNode }) {
    const [user, setUser] = useState<string | undefined>(() => getCookie('email'));
    const [role, setRole] = useState<string | undefined>(() => getCookie('role'));

    return (
        <InfoInContext.Provider value={{ email: user, setEmail: setUser, role, setRole}}>
            {children}
        </InfoInContext.Provider>
    )
}

export function useCurrentEmail() {
    return useContext(InfoInContext).email
}

export function useSetEmail() {
    return useContext(InfoInContext).setEmail
}

export function useCurrentRole() {
    return useContext(InfoInContext).role
}

export function useSetRole() {
    return useContext(InfoInContext).setRole
}
