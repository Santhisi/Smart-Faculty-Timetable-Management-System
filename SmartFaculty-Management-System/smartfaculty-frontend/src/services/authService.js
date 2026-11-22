import api from "./api";

export const login = async (username, password) => {
    const response = await api.post("/api/auth/login", {
        username: username,
        password: password,
    });

    return response.data;
};