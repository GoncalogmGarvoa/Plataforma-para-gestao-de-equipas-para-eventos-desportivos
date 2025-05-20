import * as React from "react"
import {useEffect, useState} from "react"
import {Link, Navigate} from "react-router-dom"
import {useSetUser} from "../../src/context/Authn"
import {useSetEmail} from "../../src/context/Player"
import "core-js/features/promise";


export async function authenticate(email: string, password: string): Promise<string | undefined> {
    const handleLogin = async () => {
        try {
            const response = await fetch("/arbnet/users/email", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({ email: email, password: password }),
            })
            const data = await response.json()
            if (response.ok) {
                const expirationDate = new Date();
                expirationDate.setHours(expirationDate.getHours() + 1);
                console.log("LOGIN TOKEN: ",data.token)
                document.cookie = `token=${data.token}; expires=${expirationDate.toUTCString()}; path=/;`
                document.cookie = `email=${email}; expires=${expirationDate.toUTCString()}; path=/;`
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
        email: "",
        password: "",
    })
    const [isSubmitting, setIsSubmitting] = useState(false)
    const [error, setError] = useState(undefined)
    const [redirect, setRedirect] = useState(false)
    const setUser = useSetUser()
    const [locationPath, setLocationPath] = useState("/me")
    const setEmail = useSetEmail()

    useEffect(() => {
        if (document.cookie != "") {
            // @ts-ignore
            setUser(document.cookie.split('; ').find(row => row.startsWith('token=')).split('=')[1]+"=")
            // @ts-ignore
            setEmail(document.cookie.split('; ').find(row => row.startsWith('email=')).split('=')[1])
            setRedirect(true)
        } else {
            setUser(undefined)
            setEmail(undefined)
        }
    }, [setUser, setEmail])


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
        const email = inputs.email
        const password = inputs.password
        authenticate(email, password)
            .then(res => {
                setIsSubmitting(false)
                if (res) {
                    console.log(`setUser(${res})`)
                    setUser(res)
                    setEmail(email)
                    setRedirect(true)
                    setLocationPath("/me")
                } else {
                    setError("Invalid email or password")
                }
            })
            .catch(error => {
                setIsSubmitting(false)
                setError(error.message)
            })
    }
    return (
        <div>
            <h1>Login</h1>
            <form onSubmit={handleSubmit}>
                <label>
                    Email:
                    <input type="text" name="email" value={inputs.email} onChange={handleChange}/>
                </label>
                <br/>
                <label>
                    Password:
                    <input type="password" name="password" value={inputs.password} onChange={handleChange}/>
                </label>
                <br/>
                <button type="submit">Login</button>
            </form>
            <p style={{textAlign: 'center'}}>
                Don’t have an account? <Link to="/users">Create an account</Link>
            </p>
        </div>

        // <form onSubmit={handleSubmit}>
        //     <h2>Login</h2>
        //     <fieldset disabled={isSubmitting}>
        //         <div>
        //             <label htmlFor="email">Email</label>
        //             <input id="email" type="text" name="email" value={inputs.email} onChange={handleChange}/>
        //         </div>
        //         <div>
        //             <label htmlFor="password">Password</label>
        //             <input id="password" type="password" name="password" value={inputs.password}
        //                    onChange={handleChange}/>
        //         </div>
        //         <div>
        //             <button type="submit">Login</button>
        //         </div>
        //     </fieldset>
        //
        //     {error && <p style={{color: 'red'}}>{error}</p>}
        //
        //     <hr style={{margin: '1rem 0'}}/>
        //
        //     <p style={{textAlign: 'center'}}>
        //         Don’t have an account? <Link to="/users">Create an account</Link>
        //     </p>
        // </form>

        // <form onSubmit={handleSubmit}>
        //     <h2>Login</h2>
        //     <fieldset disabled={isSubmitting}>
        //         <div>
        //             <label htmlFor="email">Email</label>
        //             <input id="email" type="text" name="email" value={inputs.email} onChange={handleChange} />
        //         </div>
        //         <div>
        //             <label htmlFor="password">Password</label>
        //             <input id="password" type="password" name="password" value={inputs.password} onChange={handleChange} />
        //         </div>
        //         <div>
        //             <button type="submit">Login</button>
        //         </div>
        //     </fieldset>
        //     {error && <p style={{ color: 'red' }}>{error}</p>}
        //
        //     <p>Don't have an account? <Link to="/users">Create an account</Link></p>
        // </form>
    )

}