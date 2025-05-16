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

/*
const getTokenFromCookie = async () => {
    try {
        const response = await fetch("/api/cookies")
        const data = await response.json()
        console.log("Cookies: ", data)
        if (response.ok) {
            return data.token
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

export function AuthnContainer({ children }: { children: React.ReactNode }) {
    //const [user, setUser] = useState(undefined)
    const [user, setUser] = useState(getTokenFromCookie())
    /*
      useEffect(() => {
          getTokenFromCookie().then((cookie => {
              if (cookie != user) {
                  setUser(cookie)
              }
          }))
      }, [user])


      //console.log(`AuthnContner: ${user}`)
     /* useEffect(() => {
          const tokenFromCookie = getTokenFromCookie()
          if (tokenFromCookie != user) {
              setUser(tokenFromCookie)
          }
      }, [user])

      */
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