"use client";
import { useState } from "react";
import { loginUser, registerUser } from "../lib/api";
import { useRouter } from "next/navigation";

export default function LoginPage() {
  const router = useRouter();

  const [email,setEmail]=useState("");
  const [password,setPassword]=useState("");
  const [username,setUsername]=useState("");

  const handleLogin = async()=>{
    try{
      const data = await loginUser(email,password);
      localStorage.setItem("token",data.token);
      router.push("/dashboard");
    }catch(err){
      alert("Login failed");
    }
  };

  const handleRegister = async()=>{
    try{
      const data = await registerUser(username,email,password);
      localStorage.setItem("token",data.token);
      router.push("/dashboard");
    }catch(err){
      alert("Register failed");
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-black text-white">
      <div className="grid md:grid-cols-2 gap-8 w-[800px]">

        <div className="p-6 border rounded-xl">
          <h2 className="text-2xl mb-4">Login</h2>
          <input className="input" placeholder="Email"
            onChange={e=>setEmail(e.target.value)} />
          <input className="input" placeholder="Password" type="password"
            onChange={e=>setPassword(e.target.value)} />
          <button className="btn" onClick={handleLogin}>Login</button>
        </div>

        <div className="p-6 border rounded-xl">
          <h2 className="text-2xl mb-4">Register</h2>
          <input className="input" placeholder="Username"
            onChange={e=>setUsername(e.target.value)} />
          <input className="input" placeholder="Email"
            onChange={e=>setEmail(e.target.value)} />
          <input className="input" placeholder="Password" type="password"
            onChange={e=>setPassword(e.target.value)} />
          <button className="btn" onClick={handleRegister}>Register</button>
        </div>

      </div>
    </div>
  );
}