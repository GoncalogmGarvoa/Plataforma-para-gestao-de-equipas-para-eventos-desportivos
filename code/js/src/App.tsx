import * as React from 'react';
import '../components/Components.css';
import '../components/SelectRole.css';
import '../components/CreateCallList.css';
import { FaBell } from "react-icons/fa";

import {
    createBrowserRouter,
    Link,
    Outlet,
    RouterProvider,
    Navigate,
} from 'react-router-dom';

import { AuthnContainer } from './context/Authn';
import { useCurrentUser } from "./context/Authn";
import { useCurrentRole, useCurrentEmail, UserContainer } from "./context/Referee";
import { RequireAuthn } from './RequireAuthn';
import { Logout } from '../components/user/Logout';

// Lazy-loaded components
const CreateUser = React.lazy(() =>
    import("../components/user/CreateUser").then(m => ({ default: m.CreateUser }))
);

const Login = React.lazy(() =>
    import("../components/user/Login").then(m => ({ default: m.Login }))
);

const Me = React.lazy(() =>
    import("../components/user/Me").then(m => ({ default: m.Me }))
);

const SelectRole = React.lazy(() =>
    import("../components/user/SelectRole").then(m => ({ default: m.SelectRole }))
);

const CreateCallList = React.lazy(() =>
    import("../components/callList/CreateCallList").then(m => ({ default: m.CreateCallList }))
);

const SearchCallListDraft = React.lazy(() =>
    import("../components/callList/SearchCallListDraft").then(m => ({ default: m.SearchCallListDraft }))
);

const EditCallList = React.lazy(() =>
    import("../components/callList/EditCallList").then(m => ({ default: m.EditCallList }))
);

const CheckCallLists = React.lazy(() =>
    import("../components/callList/CheckCallLists").then(m => ({ default: m.CheckCallLists }))
);

const CallListInfo = React.lazy(() =>
    import("../components/callList/CallListInfo").then(m => ({ default: m.CallListInfo }))
);

const AttributeRoles = React.lazy(() =>
    import("../components/user/AttributeRoles").then(m => ({ default: m.AttributeRoles }))
);

const InviteUsers = React.lazy(() =>
    import("../components/user/InviteUsers").then(m => ({ default: m.InviteUsers }))
);

const Notifications = React.lazy(() =>
    import("../components/user/Notifications").then(m => ({ default: m.Notifications }))
);

const Reports = React.lazy(() =>
    import("../components/reports/Reports").then(m => ({ default: m.Reports }))
);

const CreateReportRouter = React.lazy(() =>
    import("../components/reports/CreateReportRouter").then(m => ({ default: m.CreateReportRouter }))
);

const CreatePaymentReport = React.lazy(() =>
    import("../components/reports/CreatePaymentReport").then(m => ({ default: m.CreatePaymentReport }))
);


// Optional: Wrapper para Suspense
function SuspenseWrapper({ children }: { children: React.ReactNode }) {
    return (
        <React.Suspense fallback={<div>Carregando...</div>}>
            {children}
        </React.Suspense>
    );
}

// Proteção por role
function RequireArbitrationCouncil({ children }: { children: React.ReactNode }) {
    const currentRole = useCurrentRole();
    return currentRole === "Arbitration_Council" ? <>{children}</> : <Navigate to="/" replace />;
}

function RequireReferee({ children }: { children: React.ReactNode }) {
    const currentRole = useCurrentRole();
    return currentRole === "Referee" ? <>{children}</> : <Navigate to="/" replace />;
}

function RequireAdmin({ children }: { children: React.ReactNode }) {
    const currentRole = useCurrentRole();
    return currentRole === "Admin" ? <>{children}</> : <Navigate to="/" replace />;
}

// Router
const router = createBrowserRouter([
    {
        path: "/",
        element: (
            <AuthnContainer>
                <UserContainer>
                    <Header />
                    <Outlet />
                </UserContainer>
            </AuthnContainer>
        ),
        children: [
            {
                path: "/",
                element: <Home />,
            },
            {
                path: "/users/signup",
                element: (
                    <SuspenseWrapper>
                        <CreateUser />
                    </SuspenseWrapper>
                ),
            },
            {
                path: "/login",
                element: (
                    <SuspenseWrapper>
                        <Login />
                    </SuspenseWrapper>
                ),
            },
            {
                path: "/select-role",
                element: (
                    <RequireAuthn>
                        <SuspenseWrapper>
                            <SelectRole />
                        </SuspenseWrapper>
                    </RequireAuthn>
                ),
            },
            {
                path: "/me",
                element: (
                    <RequireAuthn>
                        <SuspenseWrapper>
                            <Me />
                        </SuspenseWrapper>
                    </RequireAuthn>
                ),
            },
            {
                path: "/create-calllist",
                element: (
                    <RequireAuthn>
                        <RequireArbitrationCouncil>
                            <SuspenseWrapper>
                                <CreateCallList />
                            </SuspenseWrapper>
                        </RequireArbitrationCouncil>
                    </RequireAuthn>
                ),
            },
            {
                path: "/search-calllist-draft",
                element: (
                    <RequireAuthn>
                        <RequireArbitrationCouncil>
                            <SuspenseWrapper>
                                <SearchCallListDraft />
                            </SuspenseWrapper>
                        </RequireArbitrationCouncil>
                    </RequireAuthn>
                ),
            },
            {
                path: "/check-callLists",
                element: (
                    <RequireAuthn>
                        <SuspenseWrapper>
                            <CheckCallLists />
                        </SuspenseWrapper>
                    </RequireAuthn>
                ),
            },
            {
                path: "/callList-info",
                element: (
                    <RequireAuthn>
                        <SuspenseWrapper>
                            <CallListInfo />
                        </SuspenseWrapper>
                    </RequireAuthn>
                ),
            },
            {
                path: "/edit-calllist/:id",
                element: (
                    <RequireAuthn>
                        <RequireArbitrationCouncil>
                            <SuspenseWrapper>
                                <EditCallList />
                            </SuspenseWrapper>
                        </RequireArbitrationCouncil>
                    </RequireAuthn>
                ),
            },
            {
                path: "/attribute-roles",
                element: (
                    <RequireAuthn>
                        <RequireAdmin>
                            <SuspenseWrapper>
                                <AttributeRoles />
                            </SuspenseWrapper>
                        </RequireAdmin>
                    </RequireAuthn>
                ),
            },
            {
                path: "/invite-users",
                element: (
                    <RequireAuthn>
                        <RequireAdmin>
                            <SuspenseWrapper>
                                <InviteUsers />
                            </SuspenseWrapper>
                        </RequireAdmin>
                    </RequireAuthn>
                ),
            },
            {
                path: "/reports",
                element: (
                    <RequireAuthn>
                        <RequireReferee>
                            <SuspenseWrapper>
                                <Reports />
                            </SuspenseWrapper>
                        </RequireReferee>
                    </RequireAuthn>
                ),
            },
            {
                path: "/reports/create/:callListId",
                element: (
                    <RequireAuthn>
                        <RequireReferee>
                            <SuspenseWrapper>
                                <CreateReportRouter />
                            </SuspenseWrapper>
                        </RequireReferee>
                    </RequireAuthn>
                ),
            },
            {
                path: "/payment-reports/create/:callListId",
                element: (
                    <RequireAuthn>
                        <RequireReferee>
                            <SuspenseWrapper>
                                <CreatePaymentReport />
                            </SuspenseWrapper>
                        </RequireReferee>
                    </RequireAuthn>
                ),
            },
            {
                path: "/logout",
                element: <Logout />,
            },
        ],
    },
]);

// App
export function App() {
    return <RouterProvider router={router} />;
}

// Home
function Home() {
    const currentEmail = useCurrentEmail();
    const currentRole = useCurrentRole();
    const isConselho = currentRole === "Arbitration_Council";
    const isAdmin = currentRole === "Admin";

    return (
        <div>
            <h1>Home</h1>
            <ol>
                {currentEmail ? (
                    <>
                        <li><Link to="/me">Eu</Link></li>
                        <li><Link to="/select-role">Mudar Perfil</Link></li>

                        {currentRole && <li><Link to="/check-callLists">Ver Convocatórias</Link></li>}

                        {isConselho && (
                            <>
                                <li><Link to="/create-calllist">Criar Convocatória</Link></li>
                                <li><Link to="/search-calllist-draft">Ver Convocatórias editáveis</Link></li>
                            </>
                        )}

                        {isAdmin && (
                            <>
                                <li><Link to="/attribute-roles">Gerir Utilizadores</Link></li>
                                <li><Link to="/invite-users">Convidar Utilizadores</Link></li>
                            </>
                        )}

                        {currentRole === "Referee" && <li><Link to="/reports">Relatórios</Link></li>}
                    </>
                ) : (
                    <li><Link to="/login">Login</Link></li>
                )}
            </ol>
        </div>
    );
}

// Header
export function Header() {
    const currentUser = useCurrentUser();
    const currentEmail = useCurrentEmail();
    const currentRole = useCurrentRole();

    const isConselho = currentRole === "Arbitration_Council";
    const isReferee = currentRole === "Referee";
    const isAdmin = currentRole === "Admin";

    return (
        <header>
            <nav>
                <ul className="horizontal-menu">
                    <li><Link to="/">Início</Link></li>
                    {currentUser ? (
                        <>
                            <li><Link to="/me">Eu</Link></li>
                            <li><Link to="/select-role">Mudar Perfil</Link></li>

                            {currentRole && <li><Link to="/check-callLists">Ver Convocatórias</Link></li>}
                            {isConselho && (
                                <>
                                    <li><Link to="/create-calllist">Criar Convocatória</Link></li>
                                    <li><Link to="/search-calllist-draft">Ver Convocatórias editáveis</Link></li>
                                </>
                            )}
                            {isAdmin && (
                                <>
                                    <li><Link to="/attribute-roles">Gerir Utilizadores</Link></li>
                                    <li><Link to="/invite-users">Convidar Utilizadores</Link></li>
                                </>
                            )}
                            {isReferee && <li><Link to="/reports">Relatórios</Link></li>}
                            <li style={{ position: "relative" }}>
                                <SuspenseWrapper>
                                    <Notifications />
                                </SuspenseWrapper>
                            </li>
                            <li><Logout /></li>
                        </>
                    ) : (
                        <li><Link to="/login">Login</Link></li>
                    )}
                </ul>
            </nav>
        </header>
    );
}
