import * as React from "react"
import {useEffect, useState} from "react"
import {Link, Navigate} from "react-router-dom"
import {useSetUser} from "../../src/context/Authn"
import {useSetEmail} from "../../src/context/Player"
import "core-js/features/promise";



export async function authenticate(email: string, password: string): Promise<string | { error: string }> {
    try {
        const response = await fetch("/arbnet/users/token", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({ email, password }),
        });

        const data = await response.json();

        if (response.ok) {
            const expirationDate = new Date();
            expirationDate.setHours(expirationDate.getHours() + 1);
            document.cookie = `token=${data.token}; expires=${expirationDate.toUTCString()}; path=/;`;
            document.cookie = `email=${email}; expires=${expirationDate.toUTCString()}; path=/;`;
            return data.token;
        } else {
            return { error: data.title || "Erro ao autenticar." };
        }
    } catch (error: any) {
        console.error("Network/login error:", error);
        return { error: "Network/login error." };
    }
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
        ev.preventDefault();
        setIsSubmitting(true);
        const { email, password } = inputs;

        authenticate(email, password)
            .then((res) => {
                setIsSubmitting(false);

                if (typeof res === "string") {
                    setUser(res);
                    setEmail(email);
                    setRedirect(true);
                    setLocationPath("/me");
                } else {
                    setError(res.error || "Login failed. Please try again.");
                }
            })
            .catch((error) => {
                setIsSubmitting(false);
                setError("Unexpected error. Please try again.");
            });
    }

    return (
        <div className="center-container">
            <div className="form-container">
                <h1 className="form-title">Welcome Back</h1>
                <form onSubmit={handleSubmit}>
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
                            placeholder="Enter your password"
                            required
                        />
                    </div>
                    <button
                        type="submit"
                        className="btn btn-primary"
                        disabled={isSubmitting}
                    >
                        {isSubmitting ? "Signing in..." : "Sign In"}
                    </button>
                </form>

                {error && (
                    <p className="error-message">
                        {error}
                    </p>
                )}

                <p className="link-text">
                    Don't have an account?{" "}
                    <Link to="/users">Create an account</Link>
                </p>
            </div>
        </div>
    )
}