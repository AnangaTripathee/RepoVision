"use client";
import { useCurrentUser } from '@/hooks/use-auth';
import React from 'react'

const DashBoardPage = () => {
  const{data:user, isLoading} = useCurrentUser()

  console.log(user, isLoading)
  return (
    <div>DashBoardPage</div>
  )
}

export default DashBoardPage