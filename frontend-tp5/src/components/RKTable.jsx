import React, { useState } from 'react'
import t from "../images/t.png"
import C from "../images/C.png"
import k1 from "../images/k1.png"
import CmK1s2 from "../images/CmK1s2.png"
import K2 from "../images/K2.png"
import CmK2s2 from "../images/CmK2s2.png"
import K3 from "../images/K3.png"
import CmK3 from "../images/CmK3.png"
import K4 from "../images/K4.png"
import CTm1 from "../images/CTm1.png"


const RKTable = ({ calculosSimulacion, params }) => {
    const equipos = calculosSimulacion.datosEquiposRK

    const [equipoSeleccionado, setEquipoSeleccionado] = useState(equipos[0]);
    

    const e = Math.E
    const h = 0.1

    const calculateRows = () => {
        const rows = [];
        let t = 0;
        let C = 0;
        const nExpo = params.nExpo
        const nSuma = params.nSuma
        const CIsZero = equipoSeleccionado.valorC === 0;

        while (C <= equipoSeleccionado.valorC) {

            const k1 = h * (nSuma + Math.pow(e, nExpo * C));
            const auxk1 = C + k1 / 2
            const k2 = h * (nSuma + Math.pow(e, nExpo * auxk1));
            const auxk2 = C + k2 / 2
            const k3 = h * (nSuma + Math.pow(e, nExpo * auxk2));
            const auxk3 = C + k3
            const k4 = h * (nSuma + Math.pow(e, nExpo * auxk3));
            const Cn = C + (k1 + 2 * k2 + 2 * k3 + k4) / 6;

            rows.push({ t, C, k1, auxk1, k2, auxk2, k3, auxk3, k4, Cn });

            C = Cn;
            t += h;
        }

        if (!CIsZero) {
            const k1 = h * (nSuma + Math.pow(e, nExpo * C));
            const auxk1 = C + k1 / 2
            const k2 = h * (nSuma + Math.pow(e, nExpo * auxk1));
            const auxk2 = C + k2 / 2
            const k3 = h * (nSuma + Math.pow(e, nExpo * auxk2));
            const auxk3 = C + k3
            const k4 = h * (nSuma + Math.pow(e, nExpo * auxk3));
            const Cn = C + (k1 + 2 * k2 + 2 * k3 + k4) / 6;

            rows.push({ t, C, k1, auxk1, k2, auxk2, k3, auxk3, k4, Cn });
        }

        return rows;
    };

    const rows = calculateRows();

    const handleSelectChange = (event) => {
        const selectedEquipo = equipos.find(
            (equipo) => equipo.idEquipo === parseInt(event.target.value)
        );
        setEquipoSeleccionado(selectedEquipo);
    };

    return (
        <>
            <div className="col-4 d-flex justify-content-center">
                <select onChange={handleSelectChange} className='form-select'>
                    {equipos.map((equipo) => (
                        <option key={equipo.idEquipo} value={equipo.idEquipo}>
                            E{equipo.idEquipo} C={equipo.valorC} N={equipo.valorN.toFixed(4)}
                        </option>
                    ))}
                </select>
            </div>
            <div className="container-fluid justify-content-center table-responsive table-container">

                <table>
                    <thead>
                    <tr>
                        <th>h</th>
                        <th>{h}</th>
                    </tr>
                    <tr>
                        <th>C</th>
                        <th>{equipoSeleccionado.valorC.toFixed(4)}</th>
                    </tr>
                    <tr>
                        <th>Ecuación</th>
                        <th>dC/dt = {params.nSuma} + e^({params.nExpo}*C)</th>
                    </tr>

                    </thead>
                </table>

                <table className='table table-bordered table-hover'>
                    <thead>
                    <tr>
                        <th className="text-center"><img src={t} alt={"t"}></img></th>
                        <th className="text-center"><img src={C}/></th>
                        <th className="text-center"><img src={k1}/></th>
                        <th className="text-center"><img src={CmK1s2}/></th>
                        <th className="text-center"><img src={K2}/></th>
                        <th className="text-center"><img src={CmK2s2}/></th>
                        <th className="text-center"><img src={K3}/></th>
                        <th className="text-center"><img src={CmK3}/></th>
                        <th className="text-center"><img src={K4}/></th>
                        <th className="text-center"><img src={CTm1}/></th>
                    </tr>
                    </thead>
                    <tbody>
                    {rows.map((row, index) => (
                        <tr key={index} className={index === rows.length - 1 ? 'table-dark' : ''}>
                            <td>{row.t.toFixed(2)}</td>
                            <td>{row.C.toFixed(6)}</td>
                            <td>{row.k1.toFixed(6)}</td>
                            <td>{row.auxk1.toFixed(6)}</td>
                            <td>{row.k2.toFixed(6)}</td>
                            <td>{row.auxk2.toFixed(6)}</td>
                            <td>{row.k3.toFixed(6)}</td>
                            <td>{row.auxk3.toFixed(6)}</td>
                            <td>{row.k4.toFixed(6)}</td>
                            <td>{row.Cn.toFixed(6)}</td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>
        </>
    )
}

export default RKTable