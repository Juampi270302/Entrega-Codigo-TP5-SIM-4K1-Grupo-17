import { ContextoSimulacionHandler } from "./contexts/ContextoSimulacion.jsx";
import TableStateVector from "./components/TableStateVector.jsx";
import Header from "./components/Header.jsx";
import SimulationParameters from "./components/SimulationParameters.jsx";
import { useState } from "react";
import RKTable from "./components/RKTable.jsx";

function App() {
    const [calculosSimulacion, setcalculosSimulacion] = useState(null);
    const [showStateTable, setShowStateTable] = useState(true);
    const [params, setParams] = useState(null);

    return (
        <ContextoSimulacionHandler>
            <div className="container-fluid body">
                <div className="row header">
                    <div className="col-12">
                        <Header></Header>
                    </div>
                </div>
                <div className="row d-flex justify-content-center body">
                    <div className="col-6 ">
                        <SimulationParameters setCalculosSimulacion={setcalculosSimulacion} setParams={setParams}></SimulationParameters>
                    </div>
                </div>
                
                <button onClick={() => setShowStateTable(true)} className="btn btn-secondary">Tabla de estados</button>
                <button onClick={() => setShowStateTable(false)} className="btn btn-secondary">Tabla de RK</button>

                <div className={`row d-flex justify-content-center body mt-4 ${showStateTable ? '' : 'd-none'}`}>
                    <div className="col-12">
                        {calculosSimulacion && <TableStateVector calculosSimulacion={calculosSimulacion} />}
                    </div>
                </div>
                <div className={`row d-flex justify-content-center body mt-4 ${showStateTable ? 'd-none' : ''}`}>
                    <div className="col-12">
                        {calculosSimulacion && <RKTable calculosSimulacion={calculosSimulacion} params={params} />}
                    </div>
                </div>
            </div>
        </ContextoSimulacionHandler>
    )
}

export default App
