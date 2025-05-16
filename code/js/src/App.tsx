import * as React from 'react'

import {createBrowserRouter, Link, Outlet, RouterProvider} from 'react-router-dom'
import { AuthnContainer} from './context/Authn'
import { CreateUser } from "../components/user/CreateUser"
import { Login } from "../components/user/Login"
import { useCurrentUser } from "./context/Authn"
import { RequireAuthn } from './RequireAuthn'
import { Logout } from '../components/user/Logout'

import '../components/Components.css'
import {useCurrentUsername} from "./context/Player";



const router = createBrowserRouter([
    {
        "path": "/",
        "element":
            <AuthnContainer>
                <Header />
                <Outlet />
            </AuthnContainer>,
        "children": [
            {
                "path": "/",
                "element": <Home/>,
            },

            {
                "path": "/users",
                "element": <CreateUser />
            },
            {
                "path": "/login",
                "element": <Login />
            },
            {
                "path": "/me",
                "element": <RequireAuthn><Me /></RequireAuthn>
            },
            {
                "path": "/logout",
                "element":<Logout />
            },

        ]
    }
])


export function App() {
    return (
        <RouterProvider router={router} />
    )
}


function Home() {
    const currentUser = useCurrentUser()
    console.log("HOME USER : ",currentUser)
    return (
        <div>
            <h1>Home</h1>
            <ol>
                {currentUser ?(
                    <>
                        <li><Link to="/me">Me</Link></li>
                    </>
                ): (<>

                    </>
                )}
            </ol>
            <Outlet />
        </div>
    )
}

export function Me() {
    const currentUsername = useCurrentUsername()
    return (
        <div>
            {`Hello ${currentUsername}!`}
        </div>
    )
}

export function Header() {
    const currentUser = useCurrentUser()
    const currentUsername = useCurrentUsername()

    return (
        <header>
            <nav>
                <ul className="horizontal-menu">
                    <li><Link to="/">Home</Link></li>
                    {currentUser ? (
                        <>
                            <li><Link to="/game">Play</Link></li>
                            <strong>{currentUsername} </strong>&nbsp;
                            <li><Logout/></li>
                        </>
                    ) : (
                        <>
                            <li><Link to="/login">Login</Link></li>
                            <li><Link to="/users">Create User</Link></li>
                        </>
                    )}
                </ul>
            </nav>
        </header>
    )
}



