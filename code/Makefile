##
## Makefile
##
## Jessa Bekker
## Oct 2017
## 

SMALL_DATA=../dataSubset/index_small

DATA=/cw/bdap/assignment1/Data/index

STOPWORDS=../stop-word-list_stanford.txt

MAX_N=2			# ngrams size
LOG_NB_BUCKETS=20	# hashSize is 2^this
NB_HASHES=10 		# count-min sketch hashing functions
THRESHOLD=0.5
LEARNING_RATE=0.0001	# perceptron learning rate


# Experiment parameters
OUT=./output/out
PERIOD=1000

SMALL_OUT=./output/small
SMALL_PERIOD=100


all: NaiveBayesFeatureHashing.class NaiveBayesCountMinSketch.class PerceptronFeatureHashing.class PerceptronCountMinSketch.class OnlineTextClassifier.class

metrics: Accuracy.class Recall.class Precision.class FPRate.class FPRate.class

OnlineTextClassifier.class: OnlineTextClassifier.java
	javac -cp .:lib/* $<

NaiveBayesFeatureHashing.class: NaiveBayesFeatureHashing.java
	javac -cp .:lib/* $<
	
NaiveBayesCountMinSketch.class: NaiveBayesCountMinSketch.java
	javac -cp .:lib/* $<
	
PerceptronFeatureHashing.class: PerceptronFeatureHashing.java
	javac -cp .:lib/* $<
	
PerceptronCountMinSketch.class: PerceptronCountMinSketch.java
	javac -cp .:lib/* $<

Accuracy.class: Accuracy.java
	javac $<

Recall.class: Recall.java
	javac $<

Precision.class: Precision.java
	javac $<

FPRate.class: FPRate.java
	javac $<

#TODO: Add compilation commands for other subclasses of OnlineTextClassifier (optional)
	

nbfh_small: NaiveBayesFeatureHashing.class
	@echo "Testing naive Bayes with feature hashing on a subset of the data"
	rm -f $(SMALL_OUT).nbfh.*
	time java -cp .:lib/* NaiveBayesFeatureHashing $(SMALL_DATA) $(STOPWORDS) $(LOG_NB_BUCKETS) $(THRESHOLD) $(SMALL_OUT) $(SMALL_PERIOD) $(MAX_N) -writeOutAllPredictions

nbfh: NaiveBayesFeatureHashing.class
	@echo "Testing naive Bayes with feature hashing on the complete data"
	rm -f $(OUT).nbfh.*
	time java -cp .:lib/* NaiveBayesFeatureHashing $(DATA) $(STOPWORDS) $(LOG_NB_BUCKETS) $(THRESHOLD) $(OUT) $(PERIOD) $(MAX_N)

nbcms_small: NaiveBayesCountMinSketch.class
	@echo "Testing naive Bayes with count-min sketch on a subset of the data"
	rm -f $(SMALL_OUT).nbcms.*
	time java -cp .:lib/* NaiveBayesCountMinSketch $(SMALL_DATA) $(STOPWORDS) $(LOG_NB_BUCKETS) $(NB_HASHES) $(THRESHOLD) $(SMALL_OUT) $(SMALL_PERIOD) $(MAX_N) -writeOutAllPredictions

nbcms: NaiveBayesCountMinSketch.class
	@echo "Testing naive Bayes with count-min sketch on the complete data"
	rm -f $(OUT).nbcms.*
	time java -cp .:lib/* NaiveBayesCountMinSketch $(DATA) $(STOPWORDS) $(LOG_NB_BUCKETS) $(NB_HASHES) $(THRESHOLD) $(OUT) $(PERIOD) $(MAX_N)

pfh_small: PerceptronFeatureHashing.class
	@echo "Testing perceptron classification with feature hashing on a subset of the data"
	rm -f $(SMALL_OUT).pfh.*
	time java -cp .:lib/* PerceptronFeatureHashing $(SMALL_DATA) $(STOPWORDS) $(LOG_NB_BUCKETS) $(LEARNING_RATE) $(SMALL_OUT) $(SMALL_PERIOD) $(MAX_N) -writeOutAllPredictions

pfh: PerceptronFeatureHashing.class
	@echo "Testing perceptron classification with feature hashing on the complete data"
	rm -f $(OUT).pfh.*
	time java -cp .:lib/* PerceptronFeatureHashing $(DATA) $(STOPWORDS) $(LOG_NB_BUCKETS) $(LEARNING_RATE) $(OUT) $(PERIOD) $(MAX_N)

pcms_small: PerceptronCountMinSketch.class
	@echo "Testing perceptron classification with count-min sketch on a subset of the data"
	rm -f $(SMALL_OUT).pcms.*
	time java -cp .:lib/* PerceptronCountMinSketch $(SMALL_DATA) $(STOPWORDS) $(LOG_NB_BUCKETS) $(NB_HASHES) $(LEARNING_RATE) $(SMALL_OUT) $(SMALL_PERIOD) $(MAX_N) -writeOutAllPredictions

pcms: PerceptronCountMinSketch.class
	@echo "Testing perceptron classification with count-min sketch on the complete data"
	rm -f $(OUT).pcms.*
	time java -cp .:lib/* PerceptronCountMinSketch $(DATA) $(STOPWORDS) $(LOG_NB_BUCKETS) $(NB_HASHES) $(LEARNING_RATE) $(OUT) $(PERIOD) $(MAX_N)

nbfh_threshold_small: NaiveBayesFeatureHashing.class
	@echo "Testing the best threshold for the NBFH classifier"
	rm -f $(SMALL_OUT).nbfh.*
	time java -cp .:lib/* NaiveBayesFeatureHashing $(SMALL_DATA) $(STOPWORDS) $(LOG_NB_BUCKETS) $(THRESHOLD_TEST) $(SMALL_OUT) $(SMALL_PERIOD) $(MAX_N) -writeOutAllPredictions threshold $(THRESHOLD_TEST)

nbfh_threshold: NaiveBayesFeatureHashing.class
	@echo "Testing the best threshold for the NBFH classifier"
	rm -f $(OUT).nbfh.*
	time java -cp .:lib/* NaiveBayesFeatureHashing $(DATA) $(STOPWORDS) $(LOG_NB_BUCKETS) $(THRESHOLD_TEST) $(OUT) $(PERIOD) $(MAX_N) -writeOutAllPredictions threshold $(THRESHOLD_TEST)

nbfh_ngrams_small: NaiveBayesFeatureHashing.class
	@echo "Testing the best ngram choice for the NBFH classifier"
	rm -f $(SMALL_OUT).nbfh.*
	time java -cp .:lib/* NaiveBayesFeatureHashing $(SMALL_DATA) $(STOPWORDS) $(LOG_NB_BUCKETS) 0.75 $(SMALL_OUT) $(SMALL_PERIOD) $(N_TEST) -writeOutAllPredictions ngrams $(N_TEST)

nbfh_ngrams: NaiveBayesFeatureHashing.class
	@echo "Testing the best ngram choice for the NBFH classifier"
	rm -f $(OUT).nbfh.*
	time java -cp .:lib/* NaiveBayesFeatureHashing $(DATA) $(STOPWORDS) $(LOG_NB_BUCKETS) 0.75 $(OUT) $(PERIOD) $(N_TEST) -writeOutAllPredictions ngrams $(N_TEST)

nbfh_buckets_small: NaiveBayesFeatureHashing.class
	@echo "Testing the best bucket choice for Feature Hashing"
	rm -f $(SMALL_OUT).nbfh.*
	time java -cp .:lib/* NaiveBayesFeatureHashing $(SMALL_DATA) $(STOPWORDS) $(LOG_BUCKETS_TEST) 0.75 $(SMALL_OUT) $(SMALL_PERIOD) $(MAX_N) -writeOutAllPredictions buckets $(LOG_BUCKETS_TEST)

nbfh_buckets: NaiveBayesFeatureHashing.class
	@echo "Testing the best bucket choice for Feature Hashing"
	rm -f $(OUT).nbfh.*
	time java -cp .:lib/* NaiveBayesFeatureHashing $(DATA) $(STOPWORDS) $(LOG_BUCKETS_TEST) 0.75 $(OUT) $(PERIOD) $(MAX_N) -writeOutAllPredictions buckets $(LOG_BUCKETS_TEST)


# nbfh_buckets demonstrate 20 is actually very good for buckets size
pfh_lrate_small: PerceptronFeatureHashing.class
	@echo "Testing the best Learning Rate for Perceptron"
	rm -f $(SMALL_OUT).nbfh.*
	time java -cp .:lib/* PerceptronFeatureHashing $(SMALL_DATA) $(STOPWORDS) 20 $(LEARNING_RATE_TEST) $(SMALL_OUT) $(SMALL_PERIOD) $(MAX_N) -writeOutAllPredictions lrate $(LEARNING_RATE_TEST)

pfh_lrate: PerceptronFeatureHashing.class
	@echo "Testing the best Learning Rate for Perceptron"
	rm -f $(OUT).nbfh.*
	time java -cp .:lib/* PerceptronFeatureHashing $(DATA) $(STOPWORDS) 20 $(LEARNING_RATE_TEST) $(OUT) $(PERIOD) $(MAX_N) -writeOutAllPredictions lrate $(LEARNING_RATE_TEST)

	
clean:
	rm *.class
