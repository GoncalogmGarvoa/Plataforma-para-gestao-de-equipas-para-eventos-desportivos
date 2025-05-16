import * as React from "react"
import {useEffect, useState} from "react"
import {Navigate} from "react-router-dom"
import {useSetUser} from "../../src/context/Authn"
import {useSetUsername} from "../../src/context/Player"
import "core-js/features/promise";


export async function authenticate(username: string, password: string): Promise<string | undefined> {
    const handleLogin = async () => {
        try {
            const response = await fetch("/api/users/token", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({ username: username, password: password }),
            })
            const data = await response.json()
            if (response.ok) {
                const expirationDate = new Date();
                expirationDate.setHours(expirationDate.getHours() + 1);
                console.log("LOGIN TOKEN: ",data.token)
                document.cookie = `token=${data.token}; expires=${expirationDate.toUTCString()}; path=/;`
                document.cookie = `username=${username}; expires=${expirationDate.toUTCString()}; path=/;`
                console.log("user token: ", data.token)
                return data.token
            } else {
                return undefined
            }
        }
        catch (error) {
            console.error("Error logging in:", error)
            throw error
        }
    }
    return await handleLogin()
}

export function Login() {
    console.log("Login")
    const [inputs, setInputs] = useState({
        username: "",
        password: "",
    })
    const [isSubmitting, setIsSubmitting] = useState(false)
    const [error, setError] = useState(undefined)
    const [redirect, setRedirect] = useState(false)
    const setUser = useSetUser()
    const [locationPath, setLocationPath] = useState("/me")
    const setUsername = useSetUsername()

    useEffect(() => {
        if (document.cookie != "") {
            // @ts-ignore
            setUser(document.cookie.split('; ').find(row => row.startsWith('token=')).split('=')[1]+"=")
            // @ts-ignore
            setUsername(document.cookie.split('; ').find(row => row.startsWith('username=')).split('=')[1])
            setRedirect(true)
        } else {
            setUser(undefined)
            setUsername(undefined)
        }
    }, [setUser, setUsername])


    if(redirect) {
        return <Navigate to={locationPath} replace={true}/>
    }

    function handleChange(ev: React.FormEvent<HTMLInputElement>) {
        const name = ev.currentTarget.name
        setInputs({ ...inputs, [name]: ev.currentTarget.value })
        setError(undefined)
    }
    function handleSubmit(ev: React.FormEvent<HTMLFormElement>) {
        ev.preventDefault()
        setIsSubmitting(true)
        const username = inputs.username
        const password = inputs.password
        authenticate(username, password)
            .then(res => {
                setIsSubmitting(false)
                if (res) {
                    console.log(`setUser(${res})`)
                    setUser(res)
                    setUsername(username)
                    setRedirect(true)
                    setLocationPath("/me")
                } else {
                    setError("Invalid username or password")
                }
            })
            .catch(error => {
                setIsSubmitting(false)
                setError(error.message)
            })
    }

    return (
        <form onSubmit={handleSubmit}>
            <h2>Login</h2>
            <fieldset disabled={isSubmitting}>
                <div>
                    <label htmlFor="username">Username</label>
                    <input id="username" type="text" name="username" value={inputs.username} onChange={handleChange} />
                </div>
                <div>
                    <label htmlFor="password">Password</label>
                    <input id="password" type="password" name="password" value={inputs.password} onChange={handleChange} />
                </div>
                <div>
                    <button type="submit">Login</button>
                </div>
            </fieldset>
            {error}
        </form>
    )
}