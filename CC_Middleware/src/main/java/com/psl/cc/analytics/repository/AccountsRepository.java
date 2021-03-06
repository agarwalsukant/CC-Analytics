package com.psl.cc.analytics.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.psl.cc.analytics.model.AccountDTO;

public interface AccountsRepository extends MongoRepository<AccountDTO, String> {
	@Query("{'user.id': ?0}")
	List<AccountDTO> getAllByUserId(String userId);

	@Query(value = "{'user.id': ?0}", fields = "{'accountName':1,'_id':0,'accountId':1}")
	public List<AccountDTO> getAllAccountNames(String userId);

}
