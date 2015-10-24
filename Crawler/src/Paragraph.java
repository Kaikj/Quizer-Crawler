import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunking;

import com.aliasi.sentences.MedlineSentenceModel;
import com.aliasi.sentences.SentenceChunker;
import com.aliasi.sentences.SentenceModel;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

import java.util.Iterator;
import java.util.Set;

public class Paragraph {

	TokenizerFactory factory;
	SentenceModel sentenceModel;
	SentenceChunker sentenceChunker;

	private Set<Chunk> sentences;
	private String slice;
	private Iterator<Chunk> it;

	public Paragraph(String text) {
		factory = IndoEuropeanTokenizerFactory.INSTANCE;
		sentenceModel = new MedlineSentenceModel();
		sentenceChunker = new SentenceChunker(factory, sentenceModel);

		Chunking chunking = sentenceChunker.chunk(text.toCharArray(), 0, text.length());
		sentences = chunking.chunkSet();
		slice = chunking.charSequence().toString();
		it = sentences.iterator();
	}

	public boolean hasNext() {
		return it.hasNext();
	}

	public String next() {
		Chunk sentence = it.next();
		int start = sentence.start();
		int end = sentence.end();
		return slice.substring(start, end);
	}
}

