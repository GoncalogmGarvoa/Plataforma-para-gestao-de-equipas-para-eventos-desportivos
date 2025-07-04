import * as React from 'react'
import '../components/Components.css'
import '../components/SelectRole.css'
import '../components/CreateCallList.css'
import { FaBell } from "react-icons/fa"


import {createBrowserRouter, Link, Outlet, RouterProvider, Navigate} from 'react-router-dom'
import { AuthnContainer} from './context/Authn'
import { CreateUser } from "../components/user/CreateUser"
import { Login } from "../components/user/Login"
import { Me } from "../components/user/Me"
import { useCurrentUser } from "./context/Authn"
import { RequireAuthn } from './RequireAuthn'
import { Logout } from '../components/user/Logout'
import { CreateCallList } from "../components/callList/CreateCallList";
import { SearchCallListDraft } from "../components/callList/SearchCallListDraft";
import { useCurrentRole } from "./context/Referee";

import {useCurrentEmail, UserContainer} from "./context/Referee";
import {SelectRole} from "../components/user/SelectRole";
import { EditCallList } from "../components/callList/EditCallList";
import {CheckCallLists} from "../components/callList/CheckCallLists";
import {CallListInfo} from "../components/callList/CallListInfo";
import {AttributeRoles} from "../components/user/AttributeRoles";
import {Notifications} from "../components/user/Notifications";

function RequireArbitrationCouncil({ children }: { children: React.ReactNode }) {
    const currentRole = useCurrentRole()
    
    if (currentRole !== "Arbitration_Council") {
        return <Navigate to="/" replace={true} />
    }
    
    return <>{children}</>
}

function RequireReferee({ children }: { children: React.ReactNode }) {
    const currentRole = useCurrentRole()

    if (currentRole !== "Referee") {
        return <Navigate to="/" replace={true} />
    }

    return <>{children}</>
}

function RequireAdmin({ children }: { children: React.ReactNode }) {
    const currentRole = useCurrentRole()

    if (currentRole !== "Admin") {
        return <Navigate to="/" replace={true} />
    }

    return <>{children}</>
}

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
                "path": "/select-role",
                "element": <RequireAuthn><SelectRole /></RequireAuthn>
            },
            {
                "path": "/Me",
                "element": <RequireAuthn><Me /></RequireAuthn>
            },
            {
                "path": "/create-callList",
                "element": <RequireAuthn><RequireArbitrationCouncil><CreateCallList /></RequireArbitrationCouncil></RequireAuthn>
            },
            {
                "path": "/search-callList-draft",
                "element": <RequireAuthn><RequireArbitrationCouncil><SearchCallListDraft /></RequireArbitrationCouncil></RequireAuthn>
            },
            {
                "path": "/check-callLists",
                "element": <RequireAuthn><CheckCallLists /></RequireAuthn>
            },
            {
                "path": "/callList-info",
                "element": <RequireAuthn><CallListInfo /></RequireAuthn>
            },
            {
                "path": "/edit-calllist/:id",
                "element": <RequireAuthn><RequireArbitrationCouncil><EditCallList /></RequireArbitrationCouncil></RequireAuthn>
            },
            {
                "path": "/attribute-roles",
                "element": <RequireAuthn><RequireAdmin ><AttributeRoles /></RequireAdmin></RequireAuthn>
            },
            {
                "path": "/logout",
                "element":<Logout />
            }
        ]
    }
])


export function App() {
    return (
        <RouterProvider router={router} />
    )
}




function Home() {
    const currentEmail = useCurrentEmail()
    const currentRole = useCurrentRole()
    
    const isConselhoDeArbitragem = currentRole === "Arbitration_Council"
    const isAdmin = currentRole === "Admin"

    return (
        <div>
            <h1>Home</h1>
            <ol>
                <li><Link to="/">Home</Link></li>
                {currentEmail ? (
                    <>
                        <li><Link to="/me">Me</Link></li>
                        <li><Link to="/check-callLists">Ver Convocações</Link></li>

                        {isConselhoDeArbitragem && (
                            <>
                                <li><Link to="/create-calllist">Criar Convocatória</Link></li>
                                <li><Link to="/search-calllist-draft">Ver Convocatórias Draft</Link></li>
                            </>
                        )}

                        {isAdmin && (
                            <li><Link to="/attribute-roles">Atribuir Roles</Link></li>
                        )}
                    </>


                ) : (
                    <>
                        {<li><Link to="/login">Login</Link></li>}
                        {<li><Link to="/users">Create User</Link></li>}
                    </>
                )}
            </ol>
        </div>
    )
}



export function Header() {
    const currentUser = useCurrentUser()
    const currentEmail = useCurrentEmail()
    const currentRole = useCurrentRole()

    const isConselhoDeArbitragem = currentRole === "Arbitration_Council"
    const isReferee = currentRole === "Referee"
    const isAdmin = currentRole === "Admin"

    return (
        <header>
            <nav>
                <ul className="horizontal-menu">
                    <li><Link to="/">Home</Link></li>
                    {currentUser ? (
                        <>
                            <li><Link to="/me">Me</Link></li>
                            {currentRole && (
                            <li><Link to="/check-callLists">Ver Convocações</Link></li>
                            )}
                            {isConselhoDeArbitragem && (
                                <>
                                    <li><Link to="/create-calllist">Criar Convocatória</Link></li>
                                    <li><Link to="/search-calllist-draft">Ver Convocatórias Draft</Link></li>
                                </>
                            )}

                            {isAdmin && (
                                <li><Link to="/attribute-roles">Atribuir Roles</Link></li>
                            )}

                            {currentRole && (
                                <li><Link to="/select-role">Mudar Perfil</Link></li>
                            )}

                            {/* Notificações */}
                            <li style={{ position: "relative" }}>
                                <Notifications />
                            </li>

                            <li><Logout /></li>
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





