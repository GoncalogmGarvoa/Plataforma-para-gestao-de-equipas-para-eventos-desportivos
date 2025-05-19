import * as React from 'react'

import {createBrowserRouter, Link, Outlet, RouterProvider} from 'react-router-dom'
import { AuthnContainer} from './context/Authn'
import { CreateUser } from "../components/user/CreateUser"
import { Login } from "../components/user/Login"
import { useCurrentUser } from "./context/Authn"
import { RequireAuthn } from './RequireAuthn'
import { Logout } from '../components/user/Logout'

import '../components/Components.css'
import {useCurrentEmail, UserContainer} from "./context/Player";



const router = createBrowserRouter([
    {
        "path": "/",
        "element":
            <AuthnContainer>
                <UserContainer>
                    <Header /><Outlet />
                </UserContainer>
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






// function Home() {
//     const currentUser = useCurrentUser()
//     console.log("HOME USER : ",currentUser)
//     return (
//         <div>
//             <h1>Home</h1>
//             <ol>
//                 {currentUser ?(
//                     <>
//                         <li><Link to="/me">Me</Link></li>
//                     </>
//                 ): (<>
//
//                     </>
//                 )}
//             </ol>
//             <Outlet />
//         </div>
//     )
// }

function Home() {
    const currentEmail = useCurrentEmail()
    return (
        <div>
            <h1>Home</h1>
            <ol>
                <li><Link to="/">Home</Link></li>
                {currentEmail ? (
                    <li><Link to="/me">Me</Link></li>
                ) : (
                    <>
                        <li><Link to="/login">Login</Link></li>
                        <li><Link to="/users">Create User</Link></li>
                    </>
                )}
            </ol>
        </div>
    )
}


export function Me() {
    const currentEmail = useCurrentEmail()
    return (
        <div>
            {`Hello ${currentEmail}!`}
        </div>
    )
}

export function Header() {
    const currentUser = useCurrentUser()
    const currentEmail = useCurrentEmail()

    return (
        <header>
            <nav>
                <ul className="horizontal-menu">
                    <li><Link to="/">Home</Link></li>
                    {currentUser ? (
                        <>
                            <strong>{currentEmail} </strong>&nbsp;
                            <li><Logout/></li>
                        </>
                    ) : (
                        <>
                            {/*<li><Link to="/login">Login</Link></li>*/}
                            {<li><Link to="/users">Create User</Link></li>}
                        </>
                    )}
                </ul>
            </nav>
        </header>
    )
}



