/// <reference types="react" />
import { useState} from "react"
import * as React from "react"
import {Navigate, useLocation} from "react-router-dom"

export function CreateUser(): JSX.Element {
    const [inputs, setInputs] = useState({
        name: "",
        phoneNumber: "",
        address: "",
        email: "",
        password: "",
        birthDate: "",
        iban: "",
    })
    const [isSubmitting, setIsSubmitting] = useState(false)
    const [error, setError] = useState<string | undefined>(undefined)
    const [redirect, setRedirect] = useState(false)
    const location = useLocation()

    if (redirect) {
        return <Navigate to={location.state?.source?.pathname || "/"} replace={true} />
    }

    function handleChange(ev: React.FormEvent<HTMLInputElement>) {
        const name = ev.currentTarget.name
        setInputs({ ...inputs, [name]: ev.currentTarget.value })
        setError(undefined)
    }

    function handleSubmit(ev: React.FormEvent<HTMLFormElement>) {
        ev.preventDefault()
        setIsSubmitting(true)

        const handleCreateUser = async () => {
            try {
                const response = await fetch("/arbnet/users/signup", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                    },
                    body: JSON.stringify(inputs),
                });

                const data = await response.json();
                setIsSubmitting(false);

                if (response.ok) {
                    setRedirect(true);
                } else {
                    setError(data.title || "Error creating User.");
                }
            } catch (error: any) {
                setIsSubmitting(false);
                setError(error.message);
                console.error("Error creating User:", error);
            }
        };
        handleCreateUser()
    }

    return (
        <div className="center-container">
            <div className="form-container">
                <h1 className="form-title">Create Account</h1>
                <form onSubmit={handleSubmit}>
                    <div className="form-group">
                        <label className="form-label" htmlFor="name">
                            Full Name
                        </label>
                        <input
                            id="name"
                            className="form-input"
                            type="text"
                            name="name"
                            value={inputs.name}
                            onChange={handleChange}
                            placeholder="Enter your full name"
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label className="form-label" htmlFor="phoneNumber">
                            Phone Number
                        </label>
                        <input
                            id="phoneNumber"
                            className="form-input"
                            type="tel"
                            name="phoneNumber"
                            value={inputs.phoneNumber}
                            onChange={handleChange}
                            placeholder="Enter your phone number"
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label className="form-label" htmlFor="address">
                            Address
                        </label>
                        <input
                            id="address"
                            className="form-input"
                            type="text"
                            name="address"
                            value={inputs.address}
                            onChange={handleChange}
                            placeholder="Enter your address"
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label className="form-label" htmlFor="email">
                            Email
                        </label>
                        <input
                            id="email"
                            className="form-input"
                            type="email"
                            name="email"
                            value={inputs.email}
                            onChange={handleChange}
                            placeholder="Enter your email"
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label className="form-label" htmlFor="password">
                            Password
                        </label>
                        <input
                            id="password"
                            className="form-input"
                            type="password"
                            name="password"
                            value={inputs.password}
                            onChange={handleChange}
                            placeholder="Create a password"
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label className="form-label" htmlFor="birthDate">
                            Birth Date
                        </label>
                        <input
                            id="birthDate"
                            className="form-input"
                            type="date"
                            name="birthDate"
                            value={inputs.birthDate}
                            onChange={handleChange}
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label className="form-label" htmlFor="iban">
                            IBAN
                        </label>
                        <input
                            id="iban"
                            className="form-input"
                            type="text"
                            name="iban"
                            value={inputs.iban}
                            onChange={handleChange}
                            placeholder="Enter your IBAN"
                            required
                        />
                    </div>

                    <button
                        type="submit"
                        className="btn btn-primary"
                        disabled={isSubmitting}
                    >
                        {isSubmitting ? "Creating Account..." : "Create Account"}
                    </button>
                </form>

                {error && (
                    <p className="error-message">
                        {error}
                    </p>
                )}
            </div>
        </div>
    )
}

/*
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

 */