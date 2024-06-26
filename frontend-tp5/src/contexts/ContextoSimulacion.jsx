import {createContext, useReducer, useState} from "react";

export const ContextoSimulacion = createContext(null)

const initialState = {
    probTA: 0.3,
    probTB: 0.25,
    probTC: 0.25,
    probTD: 0.2,
    timeTA: 2,
    timeTB: 1,
    timeTC: 3,
    timeTD: 1,
    timeMin: 5,
    timeMax: 5,
    limInfUnifTC: 20,
    limSupUnifTC: 100,
    nSuma: 0.1,
    nExpo: 0.09,
    cantTimeSim: 100,
    initTimeView: 50,
    cantSimIterations: 50
};

function reducer(params, action) {
    switch (action.type) {
        case 'setProbTA':
            return {...params, probTA: action.value};
        case 'setProbTB':
            return {...params, probTB: action.value};
        case 'setProbTC':
            return {...params, probTC: action.value};
        case 'setProbTD':
            return {...params, probTD: action.value};
        case 'setTimeTA':
            return {...params, timeTA: action.value};
        case 'setTimeTB':
            return {...params, timeTB: action.value};
        case 'setTimeTC':
            return {...params, timeTC: action.value};
        case 'setTimeTD':
            return {...params, timeTD: action.value};
        case 'setTimeMin':
            return {...params, timeMin: action.value};
        case 'setTimeMax':
            return {...params, timeMax: action.value};
        case 'setLimInfUnifTC':
            return {...params, limInfUnifTC: action.value};
        case 'setLimSupUnifTC':
            return {...params, limSupUnifTC: action.value};
        case 'setNSuma':
            return {...params, nSuma: action.value};
        case 'setNExpo':
            return {...params, nExpo: action.value};
        case 'setCantTimeSim':
            return {...params, cantTimeSim: action.value};
        case 'setInitTimeView':
            return {...params, initTimeView: action.value};
        case 'setCantSimIterations':
            return {...params, cantSimIterations: action.value};
        default:
            throw new Error();
    }
}

export const ContextoSimulacionHandler = ({children}) => {
    const [params, updateParams] = useReducer(reducer, initialState);
    return (
        <ContextoSimulacion.Provider value={{params, updateParams}}>
            {children}
        </ContextoSimulacion.Provider>
    )
}