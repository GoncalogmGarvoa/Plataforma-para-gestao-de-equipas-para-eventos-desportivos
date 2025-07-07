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

// Function to safely get token from cookie
const getCookie = (name: string): string | null => {
    const match = document.cookie.split('; ').find(row => row.startsWith(`${name}=`));
    return match ? match.split('=')[1] : null;
};

export function AuthnContainer({ children }: { children: React.ReactNode }) {
    const [user, setUser] = useState(() => {
        const token = getCookie('token');
        return token ? token + "=" : undefined;
    });

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
