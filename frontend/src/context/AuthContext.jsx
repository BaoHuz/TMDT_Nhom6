import React,{createContext,useContext,useState}from'react';
import {authService} from '../services/authService';
export const AuthContext=createContext(null);
export function AuthProvider({children}){
  const [user,setUser]=useState(()=>{try{return JSON.parse(localStorage.getItem('pool_user'))}catch{return null}});
  const saveSession=({data})=>{localStorage.setItem('pool_token',data.token);localStorage.setItem('pool_user',JSON.stringify(data));setUser(data);return data};
  const login=async data=>saveSession(await authService.login(data));
  const register=async data=>saveSession(await authService.register(data));
  const logout=()=>{localStorage.removeItem('pool_token');localStorage.removeItem('pool_user');setUser(null)};
  return <AuthContext.Provider value={{user,login,register,logout}}>{children}</AuthContext.Provider>
}
export const useAuth=()=>useContext(AuthContext);
