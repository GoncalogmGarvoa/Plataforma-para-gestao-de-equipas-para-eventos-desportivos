import React, { useEffect, useState } from "react"
import "./AttributeRoles.css"

interface Role {
    id: number
    name: string
}

interface User {
    userId: number
    userName: string
    userRoles: string[]
    status: string
}

interface Category {
    id: number
    name: string
}

interface UserDetails {
    userId: number;
    userName: string;
    userRoles: string[];
    email?: string;
    status: string;
    phoneNumber?: string;
    [key: string]: any;
}

interface UserCategoryHistory {
    categoryId: number;
    categoryName: string;
    startDate: string;
    endDate?: string | null;
}

export function AttributeRoles() {
    const [availableRoles, setAvailableRoles] = useState<Role[]>([])
    const [availableCategories, setAvailableCategories] = useState<Category[]>([])
    const [userCategories, setUserCategories] = useState<{ [userId: number]: number }>({})

    const [userNameSearch, setUserNameSearch] = useState("")
    const [selectedRoles, setSelectedRoles] = useState<string[]>([])
    const [users, setUsers] = useState<User[]>([])
    const [selectedUserDetails, setSelectedUserDetails] = useState<UserDetails | null>(null);
    const [showUserModal, setShowUserModal] = useState(false);
    const [loadingUserDetails, setLoadingUserDetails] = useState(false);
    const [userDetailsError, setUserDetailsError] = useState<string | null>(null);
    const [showCategoryHistoryModal, setShowCategoryHistoryModal] = useState(false);
    const [categoryHistory, setCategoryHistory] = useState<UserCategoryHistory[] | null>(null);
    const [loadingCategoryHistory, setLoadingCategoryHistory] = useState(false);
    const [categoryHistoryError, setCategoryHistoryError] = useState<string | null>(null);


    useEffect(() => {
        fetch("/arbnet/users/roles")
            .then(res => {
                if (!res.ok) throw new Error("Erro ao carregar roles")
                return res.json()
            })
            .then(setAvailableRoles)
            .catch(err => {
                console.error(err)
                alert("Erro ao obter lista de roles")
            })

        fetch("/arbnet/users/categories")
            .then(res => {
                if (!res.ok) throw new Error("Erro ao carregar categorias")
                return res.json()
            })
            .then(setAvailableCategories)
            .catch(err => {
                console.error(err)
                alert("Erro ao obter categorias")
            })
    }, [])

    const fetchUserCategories = async (users: User[]) => {
        const categoriesMap: { [userId: number]: number } = {}

        await Promise.all(users.map(async (user) => {
            try {
                const res = await fetch(`/arbnet/users/category?userId=${user.userId}`)
                if (!res.ok) throw new Error("Erro ao obter categoria")
                const categoryId = await res.json()
                categoriesMap[user.userId] = categoryId
            } catch (err) {
                console.error(`Erro ao obter categoria do utilizador ${user.userName}`, err)
            }
        }))

        setUserCategories(categoriesMap)
    }

    const handleSearch = () => {
        fetch(`/arbnet/users/parameters?userName=${userNameSearch}&userRoles=${selectedRoles.join(",")}`)
            .then(res => {
                if (!res.ok) throw new Error("Erro ao pesquisar utilizadores")
                return res.json()
            })
            .then(async (data) => {
                setUsers(data)
                await fetchUserCategories(data)
            })
            .catch(err => {
                console.error(err)
                alert("Erro ao obter utilizadores")
            })
    }

    const handleFetchUsersWithoutRoles = () => {
        fetch(`/arbnet/users/withoutRoles?userName=${userNameSearch}`)
            .then(res => {
                if (!res.ok) throw new Error("Erro ao obter utilizadores sem roles")
                return res.json()
            })
            .then(async (data) => {
                setUsers(data)
                await fetchUserCategories(data)
            })
            .catch(err => {
                console.error(err)
                alert("Erro ao obter utilizadores sem roles")
            })
    }

    const handleFetchInactiveUsers = () => {
        fetch("/arbnet/users/inactive")
            .then(res => {
                if (!res.ok) throw new Error("Erro ao obter utilizadores inativos")
                return res.json()
            })
            .then(data => {
                setUsers(data)
            })
            .catch(err => {
                console.error(err)
                alert("Erro ao obter utilizadores inativos")
            })
    }

    const toggleUserRole = (user: User, role: Role) => {
        const hasRole = user.userRoles.includes(role.name)

        fetch("/arbnet/users/roles", {
            method: "PUT",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                userId: user.userId,
                roleId: role.id,
                addOrRemove: !hasRole
            })
        })
            .then(res => {
                if (!res.ok) throw new Error("Erro ao atualizar roles")
                return res.json()
            })
            .then(() => {
                setUsers(prevUsers =>
                    prevUsers.map(u => {
                        if (u.userId !== user.userId) return u
                        const updatedRoles = hasRole
                            ? u.userRoles.filter(r => r !== role.name)
                            : [...u.userRoles, role.name]
                        return { ...u, userRoles: updatedRoles }
                    })
                )
            })
            .catch(err => {
                console.error(err)
                alert("Erro ao atualizar role do utilizador")
            })
    }

    const toggleUserStatus = (user: User) => {
        const newStatus = user.status === "active" ? "inactive" : "active"

        fetch("/arbnet/users/status", {
            method: "PUT",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                userId: user.userId,
                status: newStatus
            })
        })
            .then(res => {
                if (!res.ok) throw new Error("Erro ao atualizar estado do utilizador")
                return res.json()
            })
            .then(() => {
                // Atualiza localmente o status do utilizador
                setUsers(prevUsers =>
                    prevUsers.map(u =>
                        u.userId === user.userId ? { ...u, status: newStatus } : u
                    )
                )
            })
            .catch(err => {
                console.error(err)
                alert("Erro ao atualizar estado do utilizador")
            })
    }


    const handleCategoryChange = (userId: number, newCategoryId: number) => {
        fetch("/arbnet/users/category", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({ userId, categoryId: newCategoryId })
        })
            .then(res => {
                if (!res.ok) throw new Error("Erro ao atualizar categoria")
                setUserCategories(prev => ({ ...prev, [userId]: newCategoryId }))
            })
            .catch(err => {
                console.error(err)
                alert("Erro ao atualizar categoria do utilizador")
            })
    }

    const handleShowUserInfo = async (userId: number) => {
        setLoadingUserDetails(true);
        setUserDetailsError(null);
        setShowUserModal(true);
        try {
            const res = await fetch(`/arbnet/users/id/${userId}`);
            if (!res.ok) throw new Error("Erro ao obter detalhes do utilizador");
            const data = await res.json();
            setSelectedUserDetails(data);
        } catch (err: any) {
            setUserDetailsError(err.message || "Erro desconhecido");
            setSelectedUserDetails(null);
        } finally {
            setLoadingUserDetails(false);
        }
    };

    const handleShowCategoryHistory = async (userId: number) => {
        setLoadingCategoryHistory(true);
        setCategoryHistoryError(null);
        setShowCategoryHistoryModal(true);
        try {
            const res = await fetch(`/arbnet/users/historyRolesFromUser/${userId}`);
            if (!res.ok) throw new Error("Erro ao obter histórico de categorias");
            const data = await res.json();
            setCategoryHistory(data);
        } catch (err: any) {
            setCategoryHistoryError(err.message || "Erro desconhecido");
            setCategoryHistory(null);
        } finally {
            setLoadingCategoryHistory(false);
        }
    };

    return (
        <div className="attribute-roles-container">
            <h2>Gerir Utilizadores</h2>

            <div className="search-controls">
                <input
                    type="text"
                    placeholder="Pesquisar por nome"
                    value={userNameSearch}
                    onChange={(e) => setUserNameSearch(e.target.value)}
                    className="search-input"
                />

                {availableRoles.map(role => (
                    <button
                        key={role.id}
                        onClick={() =>
                            setSelectedRoles(prev =>
                                prev.includes(role.name)
                                    ? prev.filter(r => r !== role.name)
                                    : [...prev, role.name]
                            )
                        }
                        className={`role-filter-button ${selectedRoles.includes(role.name) ? "selected" : ""}`}
                    >
                        {role.name}
                    </button>
                ))}

                <button onClick={handleSearch} className="btn btn-primary">
                    Pesquisar
                </button>

                <button onClick={handleFetchUsersWithoutRoles} className="btn btn-secondary">
                    Utilizadores novos/Sem Roles
                </button>

                <button onClick={handleFetchInactiveUsers} className="btn btn-secondary">
                    Utilizadores Inativos
                </button>
            </div>

            <hr />

            <h3>Utilizadores Encontrados</h3>
            {users.length === 0 ? (
                <p className="message-info">Nenhum utilizador encontrado.</p>
            ) : (
                <table className="user-table">
                    <thead>
                    <tr>
                        <th>Nome</th>
                        <th></th>
                        <th></th>
                        <th>Perfis</th>
                        <th>Categoria</th>
                        <th>Estado</th>
                    </tr>
                    </thead>
                    <tbody>
                    {users.map(user => (
                        <tr key={user.userId}>
                            <td>{user.userName}</td>
                            <td>
                                <button onClick={() => handleShowUserInfo(user.userId)} className="btn btn-secondary">Info</button>
                            </td>
                            <td>
                                <button onClick={() => handleShowCategoryHistory(user.userId)} className="btn btn-secondary">Histórico de categorias</button>
                            </td>
                            <td>
                                {availableRoles.map(role => (
                                    <button
                                        key={role.id}
                                        onClick={() => toggleUserRole(user, role)}
                                        className={`user-role-toggle-button ${user.userRoles.includes(role.name) ? "active-role" : "inactive-role"}`}
                                    >
                                        {role.name}
                                    </button>
                                ))}
                            </td>
                            <td>
                                <select
                                    value={userCategories[user.userId] || ""}
                                    onChange={(e) => handleCategoryChange(user.userId, parseInt(e.target.value))}
                                    className="category-select"
                                >
                                    <option value="">Selecionar Categoria</option>
                                    {availableCategories.map(category => (
                                        <option key={category.id} value={category.id}>
                                            {category.name}
                                        </option>
                                    ))}
                                </select>
                            </td>
                            <td>
                                <button
                                    onClick={() => toggleUserStatus(user)}
                                    className={`user-status-toggle-button ${user.status === "active" ? "active-status" : "inactive-status"}`}
                                >
                                    {user.status === "active" ? "Desativar" : "Ativar"}
                                </button>
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            )}

            {/* Modal de detalhes do utilizador */}
            {showUserModal && (
                <div style={{
                    position: "fixed",
                    top: 0,
                    left: 0,
                    width: "100vw",
                    height: "100vh",
                    background: "rgba(0,0,0,0.3)",
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                    zIndex: 1000
                }}
                    onClick={() => setShowUserModal(false)}
                >
                    <div style={{
                        background: "#fff",
                        padding: 24,
                        borderRadius: 8,
                        minWidth: 320,
                        maxWidth: 400,
                        boxShadow: "0 2px 8px rgba(0,0,0,0.2)",
                        position: "relative"
                    }}
                        onClick={e => e.stopPropagation()}
                    >
                        <button style={{position: "absolute", top: 8, right: 8}} onClick={() => setShowUserModal(false)}>X</button>
                        <h3>Informação do Utilizador</h3>
                        {loadingUserDetails ? (
                            <p>A carregar...</p>
                        ) : userDetailsError ? (
                            <p style={{color: 'red'}}>{userDetailsError}</p>
                        ) : selectedUserDetails ? (
                            <div>
                                <p><b>Nome:</b> {selectedUserDetails.name || selectedUserDetails.userName}</p>
                                {selectedUserDetails.phoneNumber && <p><b>Número de Telemóvel:</b> {selectedUserDetails.phoneNumber}</p>}
                                {selectedUserDetails.email && <p><b>Email:</b> {selectedUserDetails.email}</p>}
                            </div>
                        ) : (
                            <p>Sem dados.</p>
                        )}
                    </div>
                </div>
            )}

            {/* Modal de histórico de categorias */}
            {showCategoryHistoryModal && (
                <div style={{
                    position: "fixed",
                    top: 0,
                    left: 0,
                    width: "100vw",
                    height: "100vh",
                    background: "rgba(0,0,0,0.3)",
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                    zIndex: 1000
                }}
                    onClick={() => setShowCategoryHistoryModal(false)}
                >
                    <div style={{
                        background: "#fff",
                        padding: 24,
                        borderRadius: 8,
                        minWidth: 320,
                        maxWidth: 500,
                        boxShadow: "0 2px 8px rgba(0,0,0,0.2)",
                        position: "relative"
                    }}
                        onClick={e => e.stopPropagation()}
                    >
                        <button style={{position: "absolute", top: 8, right: 8}} onClick={() => setShowCategoryHistoryModal(false)}>X</button>
                        <h3>Histórico de Categorias</h3>
                        {loadingCategoryHistory ? (
                            <p>A carregar...</p>
                        ) : categoryHistoryError ? (
                            <p style={{color: 'red'}}>{categoryHistoryError}</p>
                        ) : categoryHistory && categoryHistory.length > 0 ? (
                            <table style={{width: '100%', borderCollapse: 'collapse'}}>
                                <thead>
                                    <tr>
                                        <th>Categoria</th>
                                        <th>Início</th>
                                        <th>Fim</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {categoryHistory.map((h, i) => (
                                        <tr key={i}>
                                            <td>{h.categoryName}</td>
                                            <td>{h.startDate}</td>
                                            <td>{h.endDate ? h.endDate : 'Ativo'}</td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        ) : (
                            <p>Sem histórico encontrado.</p>
                        )}
                    </div>
                </div>
            )}
        </div>
    )
}
