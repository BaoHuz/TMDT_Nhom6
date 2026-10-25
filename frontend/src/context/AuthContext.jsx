import React,{createContext,useContext,useState}from'react';
import {authService} from '../services/authService';
export const AuthContext=createContext(null);
export function AuthProvider({children}){const [user,setUser]=useState(()=>{try{return JSON.parse(localStorage.getItem('pool_user'))}catch{return null}});const login=async data=>{const {data:r}=await authService.login(data);localStorage.setItem('pool_token',r.token);localStorage.setItem('pool_user',JSON.stringify(r));setUser(r);return r};const logout=()=>{localStorage.removeItem('pool_token');localStorage.removeItem('pool_user');setUser(null)};return <AuthContext.Provider value={{user,login,logout}}>{children}</AuthContext.Provider>}
export const useAuth=()=>useContext(AuthContext);
