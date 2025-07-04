import React, { useEffect, useState } from "react"

interface Role {
    id: number
    name: string
}

interface User {
    userId: number
    userName: string
    userRoles: string[]
}

interface Category {
    id: number
    name: string
}

export function AttributeRoles() {
    const [availableRoles, setAvailableRoles] = useState<Role[]>([])
    const [availableCategories, setAvailableCategories] = useState<Category[]>([])
    const [userCategories, setUserCategories] = useState<{ [userId: number]: number }>({})

    const [userNameSearch, setUserNameSearch] = useState("")
    const [selectedRoles, setSelectedRoles] = useState<string[]>([])
    const [users, setUsers] = useState<User[]>([])

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

    return (
        <div>
            <h2>User Roles</h2>

            <div style={{ marginBottom: "1em" }}>
                <input
                    type="text"
                    placeholder="Pesquisar por nome"
                    value={userNameSearch}
                    onChange={(e) => setUserNameSearch(e.target.value)}
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
                        style={{
                            marginLeft: "0.5em",
                            backgroundColor: selectedRoles.includes(role.name) ? "lightblue" : "white"
                        }}
                    >
                        {role.name}
                    </button>
                ))}

                <button onClick={handleSearch} style={{ marginLeft: "0.5em" }}>
                    Pesquisar
                </button>

                <button onClick={handleFetchUsersWithoutRoles} style={{ marginLeft: "0.5em", fontWeight: "bold" }}>
                    Utilizadores novos/Sem Roles
                </button>
            </div>

            <hr />

            <h3>Utilizadores Encontrados</h3>
            {users.length === 0 ? (
                <p>Nenhum utilizador encontrado.</p>
            ) : (
                <ul style={{ listStyle: "none", padding: 0 }}>
                    {users.map(user => (
                        <li
                            key={user.userId}
                            style={{
                                padding: "1em 0",
                                borderBottom: "1px solid #ccc"
                            }}
                        >
                            <strong>{user.userName}</strong> —{" "}
                            {user.userRoles.length > 0 ? user.userRoles.join(", ") : <em>Sem roles</em>}

                            <div style={{ marginTop: "0.5em" }}>
                                {availableRoles.map(role => {
                                    const hasRole = user.userRoles.includes(role.name)
                                    return (
                                        <button
                                            key={role.id}
                                            onClick={() => toggleUserRole(user, role)}
                                            style={{
                                                marginRight: "0.5em",
                                                backgroundColor: hasRole ? "lightgreen" : "lightgray"
                                            }}
                                        >
                                            {hasRole ? `Remover ${role.name}` : `Adicionar ${role.name}`}
                                        </button>
                                    )
                                })}
                            </div>

                            <div style={{ marginTop: "0.5em" }}>
                                <label>
                                    Categoria:
                                    <select
                                        value={userCategories[user.userId] ?? ""}
                                        onChange={(e) =>
                                            handleCategoryChange(user.userId, Number(e.target.value))
                                        }
                                        style={{ marginLeft: "0.5em" }}
                                    >
                                        <option value="" disabled>Selecionar categoria</option>
                                        {availableCategories.map(cat => (
                                            <option key={cat.id} value={cat.id}>
                                                {cat.name}
                                            </option>
                                        ))}
                                    </select>
                                </label>
                            </div>
                        </li>
                    ))}
                </ul>
            )}
        </div>
    )
}
