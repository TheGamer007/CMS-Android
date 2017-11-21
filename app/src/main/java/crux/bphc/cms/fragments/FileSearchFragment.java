package crux.bphc.cms.fragments;


import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import crux.bphc.cms.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;
import set.file_search.FileDetail;

/**
 * A simple {@link Fragment} subclass.
 */
public class FileSearchFragment extends Fragment {

    private static final String LOG_TAG = FileSearchFragment.class.getName();

    private Retrofit retrofit;
    private Call<List<FileDetail>> call;

    private Toast toast;
    private EditText fileSearchEditText;
    private ImageView fileSearchImageView;
    private RecyclerView recyclerView;

    private FileSearchAdapter adapter;

    public FileSearchFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_file_search, container, false);
        fileSearchEditText = (EditText) view.findViewById(R.id.file_search_edit_text);
        fileSearchImageView = (ImageView) view.findViewById(R.id.file_search_button);
        recyclerView = (RecyclerView) view.findViewById(R.id.file_search_container);

        adapter = new FileSearchAdapter(new ArrayList<FileDetail>());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fileSearchImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = fileSearchEditText.getText().toString();
                if (query.length() > 0) {
                    sendRequest(query);
                }
            }
        });
    }

    private interface FileSearchAPI {

        @GET("/search")
        Call<List<FileDetail>> getFiles(@Query("query")String search);

    }

    private Retrofit getAPIClient() {
        if (retrofit == null) {
            Retrofit.Builder builder = new Retrofit.Builder()
                    .baseUrl("http://fa3dfb80.ngrok.io")
                    .addConverterFactory(GsonConverterFactory.create());
            retrofit = builder.build();
        }
        return retrofit;
    }

    private void sendRequest(String query) {
        FileSearchAPI fileSearchAPI = getAPIClient().create(FileSearchAPI.class);
        if (call != null) {
            call.cancel();
        }
        call = fileSearchAPI.getFiles(query);
        call.enqueue(new Callback<List<FileDetail>>() {
            @Override
            public void onResponse(Call<List<FileDetail>> call, Response<List<FileDetail>> response) {
                List<FileDetail> fileDetails = response.body();
                if (toast != null) toast.cancel();
                if (fileDetails == null) {
                    toast = Toast.makeText(
                            getActivity(),
                            "API not working. Please try after sometime!",
                            Toast.LENGTH_SHORT);
                    toast.show();
                } else if (fileDetails.size() == 0){
                    toast = Toast.makeText(
                            getActivity(),
                            "No results found. Please check your query!",
                            Toast.LENGTH_SHORT);
                    toast.show();
                }else {
                    adapter.setFileDetailList(fileDetails);
                }
            }

            @Override
            public void onFailure(Call<List<FileDetail>> call, Throwable t) {
                Log.e(LOG_TAG, t.toString());
                Toast.makeText(
                        getActivity(),
                        "Some error occurred. Please try again!",
                        Toast.LENGTH_SHORT).show();
            }
        });

    }

    private class FileSearchAdapter
            extends RecyclerView.Adapter<FileSearchAdapter.FileSearchViewHolder> {

        private List<FileDetail> fileDetailList;

        FileSearchAdapter(List<FileDetail> fileDetails) {
            fileDetailList = fileDetails;
        }

        @Override
        public FileSearchViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View view = layoutInflater.inflate(R.layout.row_file_search, parent, false);
            return new FileSearchViewHolder(view);
        }

        @Override
        public void onBindViewHolder(FileSearchViewHolder holder, int position) {
            FileDetail fileDetail = fileDetailList.get(position);
            holder.bind(fileDetail);
        }

        @Override
        public int getItemCount() {
            return fileDetailList.size();
        }

        void setFileDetailList(List<FileDetail> fileDetails) {
            fileDetailList = fileDetails;
            notifyDataSetChanged();
        }

        class FileSearchViewHolder extends RecyclerView.ViewHolder {

            TextView courseName;
            TextView fileName;
            TextView relevanceScore;

            FileSearchViewHolder(View itemView) {
                super(itemView);
                courseName = (TextView) itemView.findViewById(R.id.course_name);
                fileName = (TextView) itemView.findViewById(R.id.filename);
                relevanceScore = (TextView) itemView.findViewById(R.id.relevance_score);
            }

            void bind(FileDetail fileDetail) {
                String[] fileLoc = fileDetail.getFile().split("/");
                int n = fileLoc.length;
                courseName.setText(fileLoc[n-2]);

                Resources resources = getResources();
                String fileNameFormat = resources.getString(R.string.filename);
                fileName.setText(String.format(fileNameFormat, fileLoc[n-1]));

                String relevanceScoreFormat = resources.getString(R.string.relevance_score);
                relevanceScore.setText(String.format(
                        relevanceScoreFormat,
                        fileDetail.getRelevanceScore()));
            }
        }
    }

}
