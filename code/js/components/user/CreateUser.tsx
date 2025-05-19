    import { useState} from "react"
    import * as React from "react"
    import {Navigate, useLocation} from "react-router-dom"

    export function CreateUser() {
        console.log("CreateUser")
        const [inputs, setInputs] = useState({
            email: "",
            password: "",
        })
        const [isSubmitting, setIsSubmitting] = useState(false)
        const [error, setError] = useState(undefined)
        const [redirect, setRedirect] = useState(false)
        const location = useLocation()
        if(redirect) {
            return <Navigate to={location.state?.source?.pathname || "/"} replace={true}/>
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
            const handleCreateUser = async () => {
                try {
                    const response = await fetch("/arbnet/users/signup", {
                        method: "POST",
                        headers: {
                            "Content-Type": "application/json",
                        },
                        body: JSON.stringify({email: email, password: password}),
                    })
                    setIsSubmitting(false)
                    const data = await response.json()
                    if (response.ok) {
                        setRedirect(true)
                    } else {
                        setError(data.error || "Error creating user.")
                    }
                }
                catch (error) {
                    setIsSubmitting(false)
                    setError(error.message)
                    console.error("Error creating user:", error);
                }
            }
            handleCreateUser()
        }

        return (
            <form onSubmit={handleSubmit}>
                <h2>Create User</h2>
                <fieldset disabled={isSubmitting}>
                    <div>
                        <label htmlFor="email">Email</label>
                        <input id="email" type="text" name="email" value={inputs.email} onChange={handleChange} />
                    </div>
                    <div>
                        <label htmlFor="password">Password</label>
                        <input id="password" type="password" name="password" value={inputs.password} onChange={handleChange} />
                    </div>
                    <div>
                        <button type="submit">Create User</button>
                    </div>
                </fieldset>
                {error}
            </form>
        )
    }