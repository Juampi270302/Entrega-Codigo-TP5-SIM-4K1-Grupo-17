import axios from "axios";

export const getSimulacion = async (data) => {
    try {
        console.log(data)
        const response = await axios.post("http://192.168.0.125:32500/api/simular", data);
        return response.data;
    } catch (error) {
        console.error(error);
        throw error;
    }
};

export const getDatosPaginados = async (page) => {
    try {
        const response = await axios.get("http://192.168.0.125:32500/api/datos", {params: {
            page: page
            }});
        return response.data;
    } catch (error) {
        console.error(error);
        throw error;
    }
}