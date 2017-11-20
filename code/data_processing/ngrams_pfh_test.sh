# use: ./script [small] threshold1 threshold2...

args=($*)
file='datasets/pfh.ngrams.csv'
origin='../output/out.pfh.csv'
command='pfh_ngrams'
echo '\c'  > $file

if [ $1 == "small" ] # for using the small dataset
then
        command='pfh_ngrams_small'
        origin='../output/small.pfh.csv'
        shift
fi


for param in $*;
do
        make -C ../ MAX_N_TEST=$param $command
        tail -n +2 $origin >> $file
done
head -n 1 $origin | cat - $file > temp && mv temp $file
