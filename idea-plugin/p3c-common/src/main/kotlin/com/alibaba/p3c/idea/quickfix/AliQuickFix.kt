/*
 * Copyright 1999-2017 Alibaba Group.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.p3c.idea.quickfix

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.psi.*

/**
 *
 *
 * @author caikang
 * @date 2017/02/27
 */
interface AliQuickFix : LocalQuickFix {

    val ruleName: String
    val onlyOnThFly: Boolean

    override fun getFamilyName(): String {
        return groupName
    }

    companion object {
        const val groupName = "Ali QuickFix"

        fun doQuickFix(
            newIdentifier: String,
            project: Project,
            psiIdentifier: PsiIdentifier
        ) {
            val offset = psiIdentifier.textOffset
            val cannotFix = psiIdentifier.parent !is PsiMember
                    && !(psiIdentifier.parent is PsiLocalVariable || psiIdentifier.parent is PsiParameter)
            if (cannotFix) {
                return
            }

            val editor = FileEditorManager.getInstance(project).selectedTextEditor ?: return
            editor.caretModel.moveToOffset(psiIdentifier.textOffset)
            val psiFile = psiIdentifier.containingFile
            commitDocumentIfNeeded(psiFile, project)
            val psiFacade = JavaPsiFacade.getInstance(project)
            val factory = psiFacade.elementFactory
            psiFile.findElementAt(offset)?.replace(factory.createIdentifier(newIdentifier))
        }

        private fun commitDocumentIfNeeded(file: PsiFile?, project: Project) {
            if (file == null) {
                return
            }
            val manager = PsiDocumentManager.getInstance(project)
            val cachedDocument = manager.getCachedDocument(file) ?: return
            manager.commitDocument(cachedDocument)
        }

    }
}
