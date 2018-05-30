#!/usr/bin/python       
# coding=utf-8
import zipfile
import shutil
import os
import shlex
import subprocess
import time
import re  
#获取当前路径
currentDir = os.getcwd()
#print("-->当前目录%s" % currentDir) 
#分支是否已经存在本地
def isBranchExist(branchname):  
	cmd = "git branch"
	r = os.popen(cmd)  
	result = r.read() 
	r.close()
	if branchname in result:
		return True
	else:
		return False
#执行cmd命令
def execCmd(cmd):  
	r = os.popen(cmd)  
	text = r.read() 
	r.close()
	return text
#init git
exist = os.path.exists(".git")
if exist==False:
	os.system("git init")
	print("--->git init")
else:
	print("--->已初始化git") 

#Open file
channel_file = 'AppSwitcherConfig.txt'
print("---->open AppSwitcherConfig.txt") 
f = open(channel_file)
#lines = f.readlines()
for line in f:
	#print("----->行内容:%s"%line)
	array = line.split(";")
	project_address = array[0]
	project_name=array[1]
	branch_name=array[2]
	branch_name = branch_name.strip()
	gitIntoPath = currentDir+"/"+project_name
	print("======================>"+project_name+" branch to %s" % branch_name)
	if project_address.find("#",0,len(project_address))!=-1:
		print("##################忽略注释:"+project_address)
	elif os.path.exists(gitIntoPath)==False:
		#进入子目录
		os.makedirs(gitIntoPath)
		#git clone into
		execCmd("git clone "+project_address+" "+gitIntoPath)
		#cd dir
		os.chdir(gitIntoPath)
		if branch_name.strip()!='' and branch_name.find("master",0,len(branch_name))==-1 and branch_name.find("tag:",0,len(branch_name))==-1:
			#switch branch
			execCmd("git checkout --track "+" origin/"+branch_name)
			execCmd("git pull")
		elif branch_name.strip()!='' and branch_name.find("tag:",0,len(branch_name))!=-1:
			#switch tag
			arrayTag = branch_name.split(":")
			branchtagname=arrayTag[1]+"-from-tags"
			#if branchname exists
			isExistBranchAtLocal=isBranchExist(branchtagname)
			if isExistBranchAtLocal is False:
				execCmd("git checkout "+arrayTag[1])
				execCmd("git checkout -b "+branchtagname)
				execCmd("git push origin "+branchtagname)
				execCmd("git branch --set-upstream-to=origin/"+branchtagname+" "+branchtagname)
			else:
				print("##################tag 本地已存在该分支，无需创建tag分支，直接切换")
				execCmd("git checkout "+branchtagname)

		#退出子目录
		os.chdir(currentDir)
	else:
		#进入子目录
		os.chdir(gitIntoPath)
		if branch_name.strip()!='' and branch_name.find("tag:",0,len(branch_name))==-1:
			#查看本地分支
			isExistBranchAtLocal=isBranchExist(branch_name)
			if isExistBranchAtLocal is False:
				execCmd("git checkout --track "+" origin/"+branch_name)
			else:
				execCmd("git checkout "+branch_name)	
			execCmd("git pull")	
		elif branch_name.strip()!='' and branch_name.find("tag:",0,len(branch_name))!=-1:
			#switch tag
			arrayTag = branch_name.split(":")
			branchtagname=arrayTag[1]+"-from-tags"
			#if branchname exists
			isExistBranchAtLocal=isBranchExist(branchtagname)
			if isExistBranchAtLocal is False:
				execCmd("git checkout "+arrayTag[1])
				execCmd("git checkout -b "+branchtagname)
				execCmd("git push origin "+branchtagname)
				execCmd("git branch --set-upstream-to=origin/"+branchtagname+" "+branchtagname)
			else:
				print("##################tag 本地已存在该分支，无需创建tag分支，直接切换")
				execCmd("git checkout "+branchtagname)
		#退出子目录
		os.chdir(currentDir)
f.close()
