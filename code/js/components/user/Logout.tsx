import * as React from "react"
import { useNavigate } from "react-router-dom"
import { useSetUser, useCurrentUser } from "../../src/context/Authn"
import {useSetEmail} from "../../src/context/Player";

export function Logout() {
    const navigate = useNavigate()
    const setUser = useSetUser()
    const setEmail = useSetEmail()
    const user = useCurrentUser()


    const handleLogout = () => {
        setUser(null);
        setEmail(null);
        // clear cookies
        const expirationDate = new Date();
        expirationDate.setHours(expirationDate.getHours() - 1);
        document.cookie = `token=; expires=${expirationDate.toUTCString()}; path=/`;
        document.cookie = `email=; expires=${expirationDate.toUTCString()}; path=/`;
        //sessionStorage.clear()
        navigate("/login")
    };

    if (!user) {
        navigate("/login")
        return null
    }

    return (
        <div>
            <button onClick={handleLogout}>Logout</button>
        </div>
    )
}