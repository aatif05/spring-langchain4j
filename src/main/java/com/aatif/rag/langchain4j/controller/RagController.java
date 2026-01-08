package com.aatif.rag.langchain4j.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.aatif.rag.langchain4j.dto.RagRequest;
import com.aatif.rag.langchain4j.service.ParagraphSearch;
import com.aatif.rag.langchain4j.service.ParagraphSearch.ParagraphMatch;
import com.aatif.rag.langchain4j.service.RagService;

@RestController
public class RagController {
    @Autowired
    private RagService ragService;
    @Autowired
    private ParagraphSearch paragraphSearch;

    @PostMapping("/query")
    public String query(@RequestBody RagRequest request) {
        return ragService.ask(request.getQuestion());
    }

    @PostMapping("/search")
    public List<ParagraphMatch> search(@RequestBody RagRequest request) {
        return paragraphSearch.search(request.getQuestion());
    }
}
